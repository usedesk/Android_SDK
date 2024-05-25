package ru.usedesk.knowledgebase_gui.screen.compose.article

import android.content.Context
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
    onWebUrl: (String) -> Unit
) : WebView(context) {
    init {
        isVerticalScrollBarEnabled = false
        settings.apply {
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            domStorageEnabled = true
        }
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
                view: WebView?,
                url: String?
            ): WebResourceResponse? = url?.let {
                try {
                    if (URI(it).scheme == "https") {
                        val okHttpRequest = Request.Builder().url(it).build()
                        val response = okHttp.newCall(okHttpRequest).execute()
                        WebResourceResponse(
                            "",
                            "",
                            response.body?.byteStream()
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        setBackgroundColor(theme.colors.listItemBackground.toArgb())
    }
}