package ru.usedesk.knowledgebase_gui.screen.article

import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.usedesk.knowledgebase_gui.compose.card
import ru.usedesk.knowledgebase_gui.compose.composeViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State

@Composable
internal fun ContentArticle(
    articleId: Long,
    onEvent: (Event) -> Unit,
    onWebUrl: (String) -> Boolean
) {
    val viewModel = composeViewModel(articleId.toString()) { ArticleViewModel(articleId) }
    val state by viewModel.modelFlow.collectAsState()
    Box(modifier = Modifier.card()) {
        when (val content = state.content) {
            is State.Content.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            is State.Content.Article -> {
                AndroidView(
                    factory = {
                        WebView(it).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(webView: WebView?, url: String?) {
                                    super.onPageFinished(webView, url)

                                    //showQuestion(articleId)//TODO
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    url: String
                                ) = onWebUrl(url)
                            }
                            when {
                                Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 -> loadData(
                                    content.articleContent.text,
                                    "text/html; charset=utf-8",
                                    "UTF-8"
                                )
                                else -> loadData(
                                    content.articleContent.text,
                                    "text/html",
                                    null
                                )
                            }
                            setBackgroundColor(Color.TRANSPARENT)
                        }
                    }
                )
            }
        }
    }
}