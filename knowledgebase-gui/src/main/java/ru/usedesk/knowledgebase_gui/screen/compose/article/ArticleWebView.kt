package ru.usedesk.knowledgebase_gui.screen.compose.article

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.toArgb
import okhttp3.Request
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import java.net.URI

internal class ArticleWebView(
    context: Context,
    viewModel: ArticleViewModel,
    theme: UsedeskKnowledgeBaseTheme,
    onWebUrl: (String) -> Unit,
    private val onScrollTo: (Int) -> Unit
) : WebView(context) {

    private val density = context.resources.displayMetrics.density

    private inner class JsBridge {
        @JavascriptInterface
        fun onAnchorClick(anchor: String) {
            val js = "(function() {" +
                    "    var element = document.getElementById('$anchor') || document.getElementsByName('$anchor')[0];" +
                    "    if (element) {" +
                    "        return element.getBoundingClientRect().y;" +
                    "    }" +
                    "    return null;" +
                    "})()"
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

    init {
        isVerticalScrollBarEnabled = false
        settings.apply {
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            domStorageEnabled = true
            javaScriptEnabled = true
        }
        addJavascriptInterface(JsBridge(), "AndroidBridge")
        webChromeClient = WebChromeClient() // needed for HTML5 video to render
        webViewClient = object : WebViewClient() {
            private val okHttp = UsedeskOkHttpClientFactory(context).createInstance()

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

                viewModel.articleShowed()
            }

            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                if (!request.isForMainFrame) return null
                val url = request.url?.toString() ?: return null
                return try {
                    when (URI(url).scheme) {
                        "https" -> {
                            val okHttpRequest = Request.Builder().url(url).build()
                            val response = okHttp.newCall(okHttpRequest).execute()
                            response.body.let { body ->
                                WebResourceResponse(
                                    body.contentType()?.toString(),
                                    response.header("content-encoding", "utf-8"),
                                    body.byteStream()
                                )
                            }
                        }
                        else -> null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        setBackgroundColor(theme.colors.listItemBackground.toArgb())
    }

    fun setHtml(html: String) {
        // cap embedded video height so the player doesn't overflow the screen in landscape
        val maxIframeHeightPx = (resources.displayMetrics.heightPixels / density * 0.8f).toInt()
        val style = "<style>iframe{max-height:${maxIframeHeightPx}px;}</style>"
        val script = """
            <script type="text/javascript">
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
                        // WebView won't derive iframe aspect-ratio from width/height attrs — do it ourselves
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
            null,
            "$style$html$script",
            "text/html",
            "UTF-8",
            null
        )
    }
}