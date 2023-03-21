package ru.usedesk.knowledgebase_gui.screen.article

import android.graphics.Color
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
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
    WebView(
        content = state.content,
        onWebUrl = onWebUrl,
        onReviewGoodClick = remember { { onReviewClick(true) } },
        onReviewBadClick = remember { { onReviewClick(false) } }
    )
}

@Composable
private fun WebView(
    content: State.Content,
    onWebUrl: (String) -> Boolean,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    val context = LocalContext.current
    val progressView = remember(context) {
        ComposeView(context).apply {
            tag = "progressView"
            setContent {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    }
    val ratingView = remember(context) {
        ComposeView(context).apply {
            tag = "ratingView"
            visibility = View.GONE
            setContent {
                ArticleRating(
                    onReviewGoodClick = onReviewGoodClick,
                    onReviewBadClick = onReviewBadClick
                )
            }
        }
    }
    val webView = remember(context) {
        WebView(context).apply {
            tag = "webView"
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            setOnTouchListener { view, event ->
                event.action == MotionEvent.ACTION_MOVE
            }
            settings.apply {
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                domStorageEnabled = true
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ) = onWebUrl(url)

                override fun onPageCommitVisible(view: WebView, url: String?) {
                    super.onPageCommitVisible(view, url)

                    progressView.visibility = View.GONE
                    ratingView.visibility = View.VISIBLE
                }
            }
            setBackgroundColor(Color.TRANSPARENT)
        }
    }
    LaunchedEffect(content) {
        if (content is State.Content.Article) {
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
    }
    AndroidView(
        modifier = Modifier
            .animateContentSize()
            .clipToBounds()
            .verticalScroll(rememberScrollState())
            .card()
            .padding(
                start = 8.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(progressView)
                addView(webView)
                addView(ratingView)
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