package ru.usedesk.knowledgebase_gui.screen.article

import android.graphics.Color
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.card
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.composeViewModel
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State

@Composable
internal fun ContentArticle(
    articleId: Long,
    onWebUrl: (String) -> Boolean,
    onReviewClick: (good: Boolean) -> Unit
) {
    val viewModel = composeViewModel(articleId.toString()) { ArticleViewModel(articleId) }
    val state by viewModel.modelFlow.collectAsState()
    Box(modifier = Modifier) {
        when (val content = state.content) {
            is State.Content.Loading -> Loading()
            is State.Content.Article -> WebView(
                content = content,
                onWebUrl = onWebUrl,
                onReviewGoodClick = remember { { onReviewClick(true) } },
                onReviewBadClick = remember { { onReviewClick(false) } }
            )
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
    onWebUrl: (String) -> Boolean,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    AndroidView(
        modifier = Modifier
            .clipToBounds()
            .verticalScroll(scrollState)
            .card()
            .padding(
                start = 8.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        factory = {
            LinearLayout(it).apply {
                orientation = LinearLayout.VERTICAL
                val composeView = ComposeView(it).apply {
                    tag = "composeView"
                    visibility = View.GONE
                    setContent {
                        ArticleRating(
                            onReviewGoodClick = onReviewGoodClick,
                            onReviewBadClick = onReviewBadClick
                        )
                    }
                }
                val webView = WebView(it).apply {
                    tag = "webView"
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    setOnTouchListener { view, event ->
                        event.action == MotionEvent.ACTION_MOVE
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            url: String
                        ) = onWebUrl(url)

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)

                            composeView.visibility = View.VISIBLE
                        }


                    }
                    setBackgroundColor(Color.TRANSPARENT)
                }
                addView(webView)
                addView(composeView)
            }
        },
        update = {
            val webView = it.findViewWithTag<WebView>("webView")
            when {
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 -> webView.loadData(
                    content.articleContent.text,
                    "text/html; charset=utf-8",
                    "UTF-8"
                )
                else -> webView.loadData(
                    content.articleContent.text,
                    "text/html",
                    null
                )
            }
        }
    )
}

@Composable
private fun ArticleRating(
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(
            start = 8.dp,
            top = 8.dp,
            bottom = 8.dp
        )
    ) {
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.usedesk_gray_2),
            thickness = 0.5.dp
        )
        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 8.dp,
                    top = 16.dp
                ),
            text = stringResource(R.string.usedesk_string_rating_question),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = colorResource(R.color.usedesk_gray_cold_2)
            )
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        color = colorResource(R.color.usedesk_green)
                            .copy(alpha = 0.25f)
                    )
                    .clickableItem(onClick = onReviewGoodClick)
                    .padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        start = 10.dp,
                        end = 10.dp
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 10.dp)
                        .size(16.dp),
                    painter = painterResource(R.drawable.usedesk_ic_rating_good),
                    contentDescription = null
                )
                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    text = stringResource(R.string.usedesk_rating_yes),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colorResource(R.color.usedesk_green)
                    )
                )
            }
            Row(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        color = colorResource(R.color.usedesk_red)
                            .copy(alpha = 0.25f)
                    )
                    .clickableItem(onClick = onReviewBadClick)
                    .padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        start = 10.dp,
                        end = 10.dp
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 10.dp)
                        .size(16.dp),
                    painter = painterResource(R.drawable.usedesk_ic_rating_bad),
                    contentDescription = null
                )
                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    text = stringResource(R.string.usedesk_rating_no),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colorResource(R.color.usedesk_red)
                    )
                )
            }
        }
    }
}