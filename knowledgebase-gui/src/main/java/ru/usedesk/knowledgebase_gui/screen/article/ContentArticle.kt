package ru.usedesk.knowledgebase_gui.screen.article

import android.graphics.Color
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.compose.card
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal const val ARTICLE_KEY = "article"

@Composable
internal fun ContentArticle(
    viewModelStoreFactory: ViewModelStoreFactory,
    articleId: Long,
    onWebUrl: (String) -> Unit,
    onReview: () -> Unit
) {
    val viewModel = kbUiViewModel(
        key = remember(articleId) { articleId.toString() },
        viewModelStoreOwner = remember { { viewModelStoreFactory.get(ARTICLE_KEY) } }
    ) { kbUiComponent -> ArticleViewModel(kbUiComponent.interactor, articleId) }
    val state by viewModel.modelFlow.collectAsState()
    state.goReview?.use { onReview() }
    Box(modifier = Modifier.fillMaxWidth()) {
        ArticleBlock(
            state = state,
            viewModel = viewModel,
            onWebUrl = onWebUrl,
            onReviewGoodClick = remember { { viewModel.onRating(true) } },
            onReviewBadClick = remember { { viewModel.onRating(false) } }
        )
    }
}

@Composable
private fun ArticleBlock(
    state: State,
    viewModel: ArticleViewModel,
    onWebUrl: (String) -> Unit,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    val context = LocalContext.current
    val articleShowed = remember { mutableStateOf(false) }
    val scrollState = when {
        articleShowed.value -> viewModel.scrollState
        else -> rememberScrollState()
    }
    val progressView = remember(context) {
        ComposeView(context).apply {
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
            visibility = View.GONE
            setContent {
                val state by viewModel.modelFlow.collectAsState()
                ArticleRating(
                    state = state,
                    onReviewGoodClick = onReviewGoodClick,
                    onReviewBadClick = onReviewBadClick
                )
            }
        }
    }
    val webView = remember(context) {
        WebView(context).apply {
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
                ): Boolean {
                    onWebUrl(url)
                    return true
                }

                override fun onPageCommitVisible(view: WebView, url: String?) {
                    super.onPageCommitVisible(view, url)

                    progressView.visibility = View.GONE
                    ratingView.visibility = View.VISIBLE

                    articleShowed.value = true
                }
            }
            setBackgroundColor(Color.TRANSPARENT)
        }
    }
    LaunchedEffect(state.loadingState) {
        if (state.loadingState is LoadingState.Loaded<UsedeskArticleContent>) {
            when {
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 -> webView.loadData(
                    state.loadingState.data.text,
                    "text/html; charset=utf-8",
                    "UTF-8"
                )
                else -> webView.loadData(
                    state.loadingState.data.text,
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
            .verticalScroll(scrollState)
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            )
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
private fun ArticleRatingButton(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    @ColorRes colorId: Int,
    @StringRes textId: Int,
    loading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = colorResource(colorId)
                    .copy(alpha = 0.25f)
            )
            .clickableItem(onClick = onClick)
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                start = 10.dp,
                end = 10.dp
            )
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(16.dp)
            )
            else -> Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(16.dp),
                painter = painterResource(iconId),
                contentDescription = null
            )
        }
        BasicText(
            modifier = Modifier
                .padding(start = 10.dp)
                .align(Alignment.CenterVertically),
            text = stringResource(textId),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colorResource(colorId)
            )
        )
    }
}

@Composable
private fun ArticleRatingButtons(
    ratingState: RatingState,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ArticleRatingButton(
            iconId = R.drawable.usedesk_ic_rating_good,
            colorId = R.color.usedesk_green,
            textId = R.string.usedesk_rating_yes,
            loading = (ratingState as? RatingState.Sending)?.good == true,
            onClick = onReviewGoodClick
        )
        ArticleRatingButton(
            modifier = Modifier.padding(start = 10.dp),
            iconId = R.drawable.usedesk_ic_rating_bad,
            colorId = R.color.usedesk_red,
            textId = R.string.usedesk_rating_no,
            loading = (ratingState as? RatingState.Sending)?.good == false,
            onClick = onReviewBadClick
        )
    }
}

@Composable
private fun ArticleRating(
    state: State,
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
        when (state.ratingState) {
            is RatingState.Required,
            is RatingState.Sending -> ArticleRatingButtons(
                ratingState = state.ratingState,
                onReviewGoodClick,
                onReviewBadClick
            )
            is RatingState.Sent -> BasicText(text = "Posebo za ocenku") //TODO
        }
    }
}