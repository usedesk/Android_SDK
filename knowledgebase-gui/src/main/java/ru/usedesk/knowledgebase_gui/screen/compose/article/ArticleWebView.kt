package ru.usedesk.knowledgebase_gui.screen.compose.article


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.toArgb
import org.json.JSONObject
import ru.usedesk.common_gui.UsedeskOnFullscreenListener
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

private const val FS_TAG = "UsedeskKbFs"

// JS is required for anchor scroll, table wrapping, iframe aspect-ratio and the cross-origin
// fullscreen workarounds. Pages can be loaded with a host base URL (UsedeskKnowledgeBaseConfiguration.urlApi)
// so cross-origin players (YouTube/RuTube) accept the embed.
@SuppressLint("SetJavaScriptEnabled")
internal class ArticleWebView(context: Context) : WebView(context) {

    private val density = context.resources.displayMetrics.density

    private var onWebUrl: (String) -> Unit = {}
    private var onScrollTo: (Int) -> Unit = {}
    private var onArticleShowed: () -> Unit = {}
    private var onArticleHidden: () -> Unit = {}

    private var boundViewModel: ArticleViewModel? = null
    private var loadedArticleId: Long? = null

    var fullscreenListener: UsedeskOnFullscreenListener? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    val isInFullscreen: Boolean get() = customView != null

    private inner class JsBridge {
        @JavascriptInterface
        fun onAnchorClick(anchor: String) {
            // Pass anchor as a JS string argument (JSONObject.quote produces a safely-escaped
            // literal) so a crafted anchor name can't break out of the quotes and inject code.
            val js =
                "(function(a){var e=document.getElementById(a)||document.getElementsByName(a)[0];" +
                        "return e?e.getBoundingClientRect().y:null;})(${JSONObject.quote(anchor)})"
            this@ArticleWebView.post {
                this@ArticleWebView.evaluateJavascript(js) {
                    val y = it.toFloatOrNull()
                    if (y != null) {
                        onScrollTo((y * density).toInt())
                    }
                }
            }
        }
    }

    private val articleChromeClient = object : WebChromeClient() {
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            if (customView != null) {
                // WebView re-entered fullscreen without a hide — discard the stale callback.
                onHideCustomView()
            }
            val container = fullscreenListener?.getFullscreenLayout()
            if (container == null) {
                callback.onCustomViewHidden()
                return
            }
            customView = view
            customViewCallback = callback
            (view.parent as? ViewGroup)?.removeView(view)
            try {
                container.addView(view, LayoutParams(MATCH_PARENT, MATCH_PARENT))
            } catch (e: Exception) {
                Log.e(FS_TAG, "onShowCustomView: addView FAILED", e)
                throw e
            }
            container.visibility = VISIBLE
            // Two host-side workarounds for cross-origin iframes:
            // 1. __usedeskForceFs — iframe.requestFullscreen() in parent DOM. If WebView accepts
            //    (we're inside the FS user-gesture chain), it sets ':fullscreen' on the iframe and
            //    CSS auto-stretches it.
            // 2. __usedeskEnterFs — manual style stretch as a fallback when requestFullscreen is
            //    rejected. Also keeps the FS swap visually instant for YouTube (otherwise there's
            //    a ~500ms flash while WebView's native ':fullscreen' kicks in).
            evaluateJavascript(
                "if (window.__usedeskForceFs) window.__usedeskForceFs();" +
                        "if (window.__usedeskEnterFs) window.__usedeskEnterFs();",
                null
            )
            fullscreenListener?.onFullscreenChanged(true)
        }

        override fun onHideCustomView() {
            val view = customView ?: return
            val container = view.parent as? ViewGroup
            container?.removeView(view)
            container?.visibility = GONE
            customView = null
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
            evaluateJavascript(
                "if (window.__usedeskForceExitFs) window.__usedeskForceExitFs();" +
                        "if (window.__usedeskExitFs) window.__usedeskExitFs();",
                null
            )
            fullscreenListener?.onFullscreenChanged(false)
        }
    }

    init {
        isVerticalScrollBarEnabled = false
        settings.apply {
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            domStorageEnabled = true
            javaScriptEnabled = true
        }
        addJavascriptInterface(JsBridge(), "AndroidBridge")
        webChromeClient = articleChromeClient
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (!request.isForMainFrame) return false
                onWebUrl(request.url.toString())
                return true
            }

            // API < 24: the WebResourceRequest overload isn't called
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                onWebUrl(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView, url: String?) {
                super.onPageCommitVisible(view, url)
                alpha = 1f
                onArticleShowed()
            }
        }
    }

    fun bind(
        viewModel: ArticleViewModel,
        onWebUrl: (String) -> Unit,
        onScrollTo: (Int) -> Unit
    ) {
        boundViewModel = viewModel
        this.onWebUrl = onWebUrl
        this.onScrollTo = onScrollTo
        this.onArticleShowed = { viewModel.articleShowed() }
        this.onArticleHidden = { viewModel.articleHidden() }
        if (loadedArticleId != viewModel.articleId) alpha = 0f
        onResume()
    }

    fun unbind(viewModel: ArticleViewModel) {
        if (boundViewModel !== viewModel) return // stale unbind: another composition rebound us
        boundViewModel = null
        onWebUrl = {}
        onScrollTo = {}
        onArticleShowed = {}
        onArticleHidden = {}
        onPause() // keep DOM; just pause playback so re-bind resumes
    }

    /** Stop embedded media and force a fresh load next time setHtml is called. */
    fun stopMedia() {
        loadedArticleId = null
        // WebView.onPause() doesn't actually pause cross-origin iframe players —
        // reload the iframe src to truly stop streaming.
        evaluateJavascript(
            "document.querySelectorAll('iframe').forEach(function(f){" +
                    "var s=f.src;f.src='about:blank';f.src=s;" +
                    "});",
            null
        )
    }

    fun exitFullscreen(): Boolean {
        if (customView == null) return false
        articleChromeClient.onHideCustomView()
        return true
    }

    /** Detach the fullscreen custom view from its current container without ending fullscreen. */
    fun detachCustomView() {
        (customView?.parent as? ViewGroup)?.removeView(customView)
    }

    /** Reattach the cached custom view to a new container — used to resume fullscreen across Activity recreate. */
    fun reattachCustomView(container: ViewGroup) {
        val view = customView ?: return
        (view.parent as? ViewGroup)?.removeView(view)
        try {
            container.addView(view, LayoutParams(MATCH_PARENT, MATCH_PARENT))
            container.visibility = VISIBLE
        } catch (e: IllegalStateException) {
            // addView can fail in pathological host setups; bail out of fullscreen cleanly.
            Log.e(FS_TAG, "reattachCustomView: addView FAILED, exiting FS", e)
            exitFullscreen()
        }
    }

    fun applyTheme(theme: UsedeskKnowledgeBaseTheme) {
        setBackgroundColor(theme.colors.listItemBackground.toArgb())
    }

    fun setHtml(articleId: Long, html: String, baseUrl: String?) {
        if (loadedArticleId == articleId) {
            alpha = 1f
            onArticleShowed() // onPageCommitVisible won't fire for cached DOM
            return
        }
        alpha = 0f
        onArticleHidden()
        loadedArticleId = articleId
        // Cap embedded video height so the player doesn't overflow the screen in landscape.
        // useWideViewPort is left at its default (false), so 1 CSS px == 1 dp in this WebView.
        val maxIframeHeightDp = (resources.displayMetrics.heightPixels / density * 0.8f).toInt()
        val style = "<style>iframe{max-height:${maxIframeHeightDp}px;}</style>"
        val script = """
            <script type="text/javascript">
                // Manual fallback for cross-origin iframe fullscreen: WebView fires onShowCustomView
                // but never sets ':fullscreen' on the iframe in our document for non-HTML5-FS
                // players (VK uses an in-iframe CSS trick), and for YouTube the ':fullscreen' state
                // arrives ~500ms late, giving a "1-frame VK-style" flash. Stretching the iframe
                // ourselves makes the FS swap visually instant.
                window.__usedeskFsRestore = [];
                window.__usedeskEnterFs = function() {
                    var iframes = document.querySelectorAll('iframe');
                    for (var i = 0; i < iframes.length; i++) {
                        var frame = iframes[i];
                        var rect = frame.getBoundingClientRect();
                        if (rect.bottom <= 0 || rect.top >= window.innerHeight) continue;
                        window.__usedeskFsRestore.push({frame: frame, cssText: frame.style.cssText});
                        frame.style.cssText = frame.style.cssText +
                            ';position:fixed!important;top:0!important;left:0!important;right:0!important;bottom:0!important;' +
                            'width:100vw!important;height:100vh!important;max-height:none!important;z-index:2147483647!important;';
                    }
                };
                window.__usedeskExitFs = function() {
                    var restore = window.__usedeskFsRestore || [];
                    for (var i = 0; i < restore.length; i++) {
                        restore[i].frame.style.cssText = restore[i].cssText;
                    }
                    window.__usedeskFsRestore = [];
                };
                // Ask the parent document to register the visible iframe as :fullscreen so CSS
                // auto-stretches it. Helps cross-origin players whose in-iframe FS doesn't escalate
                // to the parent doc.
                window.__usedeskForceFs = function() {
                    var iframes = document.querySelectorAll('iframe');
                    for (var i = 0; i < iframes.length; i++) {
                        var f = iframes[i];
                        var r = f.getBoundingClientRect();
                        if (r.bottom <= 0 || r.top >= window.innerHeight) continue;
                        try {
                            if (f.requestFullscreen) { f.requestFullscreen(); break; }
                            if (f.webkitRequestFullscreen) { f.webkitRequestFullscreen(); break; }
                        } catch (e) {}
                    }
                };
                window.__usedeskForceExitFs = function() {
                    try {
                        if (document.exitFullscreen && document.fullscreenElement) {
                            document.exitFullscreen();
                        } else if (document.webkitExitFullscreen && document.webkitFullscreenElement) {
                            document.webkitExitFullscreen();
                        }
                    } catch (e) {}
                };
                document.addEventListener('click', function(event) {
                    var target = event.target;
                    while (target && target.tagName !== 'A') {
                        target = target.parentNode;
                    }
                    if (target && target.href) {
                        var href = target.getAttribute('href');
                        if (href.startsWith('#')) {
                            event.preventDefault();
                            AndroidBridge.onAnchorClick(href.substring(1));
                        }
                    }
                });
                (function() {
                    try {
                        // wrap tables so only the table scrolls sideways, not the page
                        var tables = document.querySelectorAll('table');
                        for (var i = 0; i < tables.length; i++) {
                            var table = tables[i];
                            var parent = table.parentNode;
                            if (!parent) continue;
                            if (parent.getAttribute && parent.getAttribute('data-usedesk-table-scroll') === '1') continue;
                            var wrap = document.createElement('div');
                            wrap.setAttribute('data-usedesk-table-scroll', '1');
                            wrap.style.overflowX = 'auto';
                            parent.insertBefore(wrap, table);
                            wrap.appendChild(table);
                        }
                    } catch (e) {}
                    try {
                        // WebView won't derive iframe aspect-ratio from width/height attrs — do it ourselves.
                        // NB: allowfullscreen / allow="fullscreen" are NOT patched here — those must be
                        // present on the parsed iframe element (see KbRepository.injectIframePermissions).
                        var iframes = document.querySelectorAll('iframe');
                        for (var j = 0; j < iframes.length; j++) {
                            var frame = iframes[j];
                            var fw = parseFloat(frame.getAttribute('width'));
                            var fh = parseFloat(frame.getAttribute('height'));
                            if (fw > 0 && fh > 0) frame.style.aspectRatio = fw + ' / ' + fh;
                        }
                    } catch (e) {}
                })();
            </script>
        """.trimIndent()
        loadDataWithBaseURL(
            baseUrl,
            "$style$html$script",
            "text/html",
            "UTF-8",
            null
        )
    }
}
