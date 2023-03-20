package ru.usedesk.knowledgebase_gui.screen.article

import android.graphics.Color
import android.os.Build
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
    Box(modifier = Modifier) {
        when (val content = state.content) {
            is State.Content.Loading -> Loading()
            is State.Content.Article -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        WebView(
                            content,
                            onWebUrl
                        )
                    }
                    Box(
                        modifier = Modifier
                            .card()
                    ) {
                        BasicText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            text = "some content"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Loading() { //TODO: можно взять ту же, что и в загрузке БЗ
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun WebView(
    content: State.Content.Article,
    onWebUrl: (String) -> Boolean
) {
    val scrollState = rememberScrollState()
    AndroidView(
        modifier = Modifier
            .clipToBounds()
            .verticalScroll(scrollState)
            .card(),
        factory = {
            WebView(it).apply {
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setOnTouchListener { view, event ->
                    event.action == MotionEvent.ACTION_MOVE
                }
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
                setBackgroundColor(Color.TRANSPARENT)
            }
        },
        update = {
            when {
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 -> it.loadData(
                    content.articleContent.text,
                    "text/html; charset=utf-8",
                    "UTF-8"
                )
                else -> it.loadData(
                    content.articleContent.text,
                    "text/html",
                    null
                )
            }
        }
    )
}