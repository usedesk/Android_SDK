
package ru.usedesk.knowledgebase_gui.screen.compose

import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.derivedStateOf
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
    val state = viewModel.modelFlow.collectAsState()
    // Per-field derivedStateOf keeps ComposeRoot's snapshot subscription narrow: it invalidates
    // only when these specific fields actually change, instead of on every VM emission.
    // Typing in the search bar mutates state.blocksState.searchText (not read here), so this
    // scope no longer recomposes per keystroke.
    val clearFocus by remember { derivedStateOf { state.value.clearFocus } }
    val screen by remember { derivedStateOf { state.value.screen } }
    val blockTitle by remember { derivedStateOf { state.value.blocksState.block.title } }
    val searchMode by remember {
        derivedStateOf {
            state.value.screen is Screen.Blocks &&
                    state.value.blocksState.block is RootViewModel.State.BlocksState.Block.Search
        }
    }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(clearFocus) { clearFocus?.use { focusManager.clearFocus() } }

    val onEvent = viewModel::onEvent
    val articleTitleState = remember { mutableStateOf<String?>(null) }
    val title = when (val s = screen) {
        Screen.Blocks -> blockTitle
        is Screen.Review -> stringResource(theme.strings.articleReviewTitle)
        is Screen.Article -> s.title ?: articleTitleState.value
        else -> s.title
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
            searchMode = searchMode,
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
                screen = screen,
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
                visible = remember { { supportButtonVisible.value } },
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
    screen: Screen,
    articleTitleState: MutableState<String?>,
    articleViews: ArticleViewsHolder,
    onSupportButtonVisibleChange: (Boolean) -> Unit,
    onWebUrl: (String) -> Unit,
    onEvent: (RootViewModel.Event) -> Unit
) {
    val forwardTransitionSpec = remember {
        slideInHorizontally(theme.animationSpec()) { it } togetherWith
                slideOutHorizontally(theme.animationSpec()) { -it }
    }
    val backwardTransitionSpec = remember {
        slideInHorizontally(theme.animationSpec()) { -it } togetherWith
                slideOutHorizontally(theme.animationSpec()) { it }
    }
    val noneTransitionSpec = remember {
        fadeIn(theme.animationSpec()) togetherWith
                fadeOut(theme.animationSpec())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = screen,
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