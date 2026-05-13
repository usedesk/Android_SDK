
package ru.usedesk.knowledgebase_gui.screen.compose

import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import ru.usedesk.knowledgebase_gui.compose.CardCircleChat
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screen.ComposeUtils.insetsHorizontal
import ru.usedesk.knowledgebase_gui.screen.ComposeUtils.insetsStatusBar
import ru.usedesk.knowledgebase_gui.screen.RootViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State.Screen
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.article.ArticleViewsHolder
import ru.usedesk.knowledgebase_gui.screen.compose.article.ContentArticle
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.ContentBlocks
import ru.usedesk.knowledgebase_gui.screen.compose.incorrect.ContentIncorrect
import ru.usedesk.knowledgebase_gui.screen.compose.loading.ContentLoading
import ru.usedesk.knowledgebase_gui.screen.compose.review.ContentReview

@Composable
internal fun ComposeRoot(
    theme: UsedeskKnowledgeBaseTheme,
    isSupportButtonVisible: Boolean,
    viewModel: RootViewModel,
    articleViews: ArticleViewsHolder,
    onBackPressed: () -> Unit,
    onGoSupport: () -> Unit,
    onWebUrl: (String) -> Unit
) {
    val state by viewModel.modelFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    val clearFocus = state.clearFocus
    LaunchedEffect(clearFocus) { clearFocus?.use { focusManager.clearFocus() } }

    val onEvent = viewModel::onEvent
    val articleTitleState = remember { mutableStateOf<String?>(null) }
    val title = when (val screen = state.screen) {
        Screen.Blocks -> state.blocksState.block.title
        is Screen.Review -> stringResource(theme.strings.articleReviewTitle)
        is Screen.Article -> screen.title ?: articleTitleState.value
        else -> screen.title
    } ?: stringResource(theme.strings.sectionsTitle)

    val scrollBehavior = rememberToolbarScrollBehavior(theme)
    val supportButtonVisible = remember { mutableStateOf(true) }

    LaunchedEffect(theme) { articleViews.webView.applyTheme(theme) }
    DisposableEffect(Unit) {
        onDispose {
            (articleViews.webView.parent as? ViewGroup)?.removeView(articleViews.webView)
            (articleViews.ratingView.parent as? ViewGroup)?.removeView(articleViews.ratingView)
        }
    }

    // Column (not Scaffold): collapsing the toolbar resizes the content via layout, not by re-subcomposing it.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        CustomToolbar(
            theme = theme,
            title = title,
            scrollBehavior = scrollBehavior,
            searchMode = state.screen is Screen.Blocks &&
                    state.blocksState.block is RootViewModel.State.BlocksState.Block.Search,
            onBackPressed = onBackPressed,
            onSearchModeEntered = remember(viewModel) {
                { viewModel.onEvent(RootViewModel.Event.SearchBarAnimationFinished) }
            },
            modifier = Modifier
                .background(color = theme.colors.rootBackground)
                .insetsStatusBar(theme)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .insetsHorizontal(theme)
                .background(color = theme.colors.rootBackground)
        ) {
            Content(
                theme = theme,
                viewModel = viewModel,
                state = state,
                articleTitleState = articleTitleState,
                articleViews = articleViews,
                onSupportButtonVisibleChange = remember {
                    { visible ->
                        supportButtonVisible.value = visible
                    }
                },
                onWebUrl = onWebUrl,
                onEvent = onEvent
            )
            CardCircleChat(
                theme = theme,
                isSupportButtonVisible = isSupportButtonVisible,
                visible = supportButtonVisible.value,
                onClicked = onGoSupport
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Content(
    theme: UsedeskKnowledgeBaseTheme,
    viewModel: RootViewModel,
    state: RootViewModel.State,
    articleTitleState: MutableState<String?>,
    articleViews: ArticleViewsHolder,
    onSupportButtonVisibleChange: (Boolean) -> Unit,
    onWebUrl: (String) -> Unit,
    onEvent: (RootViewModel.Event) -> Unit
) {
    val forwardTransitionSpec = remember {
        slideInHorizontally(theme.animationSpec()) { it } with
                slideOutHorizontally(theme.animationSpec()) { -it }
    }
    val backwardTransitionSpec = remember {
        slideInHorizontally(theme.animationSpec()) { -it } with
                slideOutHorizontally(theme.animationSpec()) { it }
    }
    val noneTransitionSpec = remember {
        fadeIn(theme.animationSpec()) with
                fadeOut(theme.animationSpec())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = state.screen,
            transitionSpec = {
                when (targetState.transition(initialState)) {
                    RootViewModel.State.Transition.FORWARD -> forwardTransitionSpec
                    RootViewModel.State.Transition.BACKWARD -> backwardTransitionSpec
                    else -> noneTransitionSpec
                }
            }) { screen ->
            val getCurrentScreen = remember { { viewModel.modelFlow.value.screen } }
            Box(modifier = Modifier.fillMaxSize()) {
                when (screen) {
                    is Screen.Loading -> ContentLoading(
                        theme = theme,
                        getCurrentScreen = getCurrentScreen,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        tryAgain = remember { { onEvent(RootViewModel.Event.TryAgain) } }
                    )
                    is Screen.Incorrect -> ContentIncorrect(theme = theme)
                    is Screen.Blocks -> ContentBlocks(
                        theme = theme,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        viewModel = viewModel,
                        onSupportButtonVisibleChange = onSupportButtonVisibleChange,
                        onEvent = onEvent
                    )
                    is Screen.Article -> ContentArticle(
                        theme = theme,
                        getCurrentScreen = getCurrentScreen,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        articleId = screen.articleId,
                        articleTitleState = articleTitleState,
                        articleViews = articleViews,
                        onSupportButtonVisibleChange = onSupportButtonVisibleChange,
                        onWebUrl = onWebUrl,
                        onReview = remember { { onEvent(RootViewModel.Event.GoReview(screen.articleId)) } }
                    )
                    is Screen.Review -> ContentReview(
                        theme = theme,
                        getCurrentScreen = getCurrentScreen,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        articleId = screen.articleId,
                        onSupportButtonVisibleChange = onSupportButtonVisibleChange,
                        goBack = viewModel::onBackPressed
                    )
                }
            }
        }
    }
}