package ru.usedesk.knowledgebase_gui.screen.compose.article

import android.content.Context
import android.webkit.JavascriptInterface
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
        webViewClient = object : WebViewClient() {
            private val okHttp = UsedeskOkHttpClientFactory(context).createInstance()

            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {
                onWebUrl(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView, url: String?) {
                super.onPageCommitVisible(view, url)

                viewModel.articleShowed()
            }

            override fun shouldInterceptRequest(
                view: WebView,
                url: String?
            ): WebResourceResponse? = url?.let {
                try {
                    when (URI(it).scheme) {
                        "https" -> {
                            val okHttpRequest = Request.Builder().url(it).build()
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
            </script>
        """.trimIndent()
        loadDataWithBaseURL(
            null,
            "$html$script",
            "text/html",
            "UTF-8",
            null
        )
    }
}