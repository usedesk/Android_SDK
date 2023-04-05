package ru.usedesk.knowledgebase_gui.screen.compose.article

import android.os.Build
import android.view.MotionEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.article.ArticleViewModel.State

internal const val ARTICLE_KEY = "article"

@Composable
internal fun ContentArticle(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreFactory: ViewModelStoreFactory,
    articleId: Long,
    supportButtonVisible: MutableState<Boolean>,
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
            theme = theme,
            state = state,
            viewModel = viewModel,
            supportButtonVisible = supportButtonVisible,
            onWebUrl = onWebUrl,
            onReviewGoodClick = remember { { viewModel.onRating(true) } },
            onReviewBadClick = remember { { viewModel.onRating(false) } }
        )
    }
}

@Composable
private fun ArticleBlock(
    theme: UsedeskKnowledgeBaseTheme,
    state: State,
    viewModel: ArticleViewModel,
    supportButtonVisible: MutableState<Boolean>,
    onWebUrl: (String) -> Unit,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state.contentState) { contentState ->
            when (contentState) {
                is ContentState.Empty -> {
                    supportButtonVisible.value = true
                    Box(modifier = Modifier.fillMaxSize())
                }
                is ContentState.Error -> {
                    supportButtonVisible.value = true
                    ScreenNotLoaded(
                        theme = theme,
                        tryAgain = if (!state.loading) viewModel::tryAgain else null
                    )
                }
                else -> {
                    val context = LocalContext.current
                    val ratingView = remember(context) {
                        ComposeView(context).apply {
                            setContent {
                                val state by viewModel.modelFlow.collectAsState()
                                AnimatedVisibility(
                                    visible = state.articleShowed,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    ArticleRating(
                                        theme = theme,
                                        state = state,
                                        onReviewGoodClick = onReviewGoodClick,
                                        onReviewBadClick = onReviewBadClick
                                    )
                                }
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

                                    viewModel.articleShowed()
                                }
                            }
                            setBackgroundColor(theme.colors.listItemBackground.toArgb())
                        }
                    }
                    LaunchedEffect(state.contentState) {
                        if (state.contentState is ContentState.Loaded) {
                            when {
                                Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 -> webView.loadData(
                                    state.contentState.content.text,
                                    "text/html; charset=utf-8",
                                    "UTF-8"
                                )
                                else -> webView.loadData(
                                    state.contentState.content.text,
                                    "text/html",
                                    null
                                )
                            }
                        }
                    }
                    val scrollState = when {
                        state.articleShowed -> state.scrollState
                        else -> rememberScrollState()
                    }
                    DisposableEffect(Unit) {
                        onDispose { viewModel.articleHidden() }
                    }
                    supportButtonVisible.value =
                        scrollState.value == 0 || scrollState.value < scrollState.maxValue
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
                            .card(theme)
                            .padding(
                                start = 8.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ),
                        factory = { context ->
                            LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL

                                addView(webView)
                                addView(ratingView)
                            }
                        }
                    )
                }
            }
        }
        CardCircleProgress(
            theme = theme,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            loading = state.loading
        )
    }
}

@Composable
private fun ArticleRatingButton(
    modifier: Modifier = Modifier,
    theme: UsedeskKnowledgeBaseTheme,
    ratingState: RatingState,
    good: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color = remember(good) {
                when {
                    good -> theme.colors.articleRatingGoodBackground
                    else -> theme.colors.articleRatingBadBackground
                }
            })
            .clickableItem(
                enabled = ratingState is RatingState.Required,
                onClick = onClick
            )
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                start = 10.dp,
                end = 10.dp
            )
    ) {
        val error = (ratingState as? RatingState.Required)?.error == good
        val loading = (ratingState as? RatingState.Sending)?.good == good
        Crossfade(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(16.dp),
            targetState = remember(error, loading) { Pair(error, loading) }
        ) {
            when {
                it.first -> Icon(
                    painter = painterResource(theme.drawables.iconRatingError),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                it.second -> CircularProgressIndicator(
                    strokeWidth = theme.dimensions.progressBarStrokeWidth,
                    color = theme.colors.progressBarIndicator
                )
                else -> Icon(
                    painter = painterResource(
                        when {
                            good -> theme.drawables.iconRatingGood
                            else -> theme.drawables.iconRatingBad
                        }
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
        BasicText(
            modifier = Modifier
                .padding(start = 10.dp)
                .align(Alignment.CenterVertically),
            text = stringResource(
                when {
                    good -> theme.strings.articleReviewYes
                    else -> theme.strings.articleReviewNo
                }
            ),
            style = when {
                good -> theme.textStyles.articleRatingGood
                else -> theme.textStyles.articleRatingBad
            }
        )
    }
}

@Composable
private fun ArticleRatingButtons(
    theme: UsedeskKnowledgeBaseTheme,
    ratingState: RatingState,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ArticleRatingButton(
            theme = theme,
            ratingState = ratingState,
            good = true,
            onClick = onReviewGoodClick
        )
        ArticleRatingButton(
            modifier = Modifier.padding(start = 10.dp),
            theme = theme,
            ratingState = ratingState,
            good = false,
            onClick = onReviewBadClick
        )
    }
}

@Composable
private fun ArticleRating(
    theme: UsedeskKnowledgeBaseTheme,
    state: State,
    onReviewGoodClick: () -> Unit,
    onReviewBadClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(
            top = 8.dp,
            bottom = 8.dp,
            start = 8.dp,
            end = 8.dp
        )
    ) {
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = theme.colors.articleRatingDivider,
            thickness = 0.5.dp
        )
        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    bottom = 8.dp
                ),
            text = stringResource(theme.strings.articleRating),
            style = theme.textStyles.articleRatingTitle
        )
        when (state.ratingState) {
            is RatingState.Required,
            is RatingState.Sending -> ArticleRatingButtons(
                theme = theme,
                ratingState = state.ratingState,
                onReviewGoodClick = onReviewGoodClick,
                onReviewBadClick = onReviewBadClick
            )
            is RatingState.Sent -> BasicText(
                text = stringResource(theme.strings.articleRatingThanks),
                style = theme.textStyles.articleRatingThanks
            )
        }
    }
}