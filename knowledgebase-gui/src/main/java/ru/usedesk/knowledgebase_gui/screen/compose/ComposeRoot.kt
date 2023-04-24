
package ru.usedesk.knowledgebase_gui.screen.compose

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import ru.usedesk.knowledgebase_gui.compose.CardCircleChat
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screen.RootViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State.Screen
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.article.ContentArticle
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.ContentBlocks
import ru.usedesk.knowledgebase_gui.screen.compose.incorrect.ContentIncorrect
import ru.usedesk.knowledgebase_gui.screen.compose.loading.ContentLoading
import ru.usedesk.knowledgebase_gui.screen.compose.review.ContentReview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComposeRoot(
    theme: UsedeskKnowledgeBaseTheme,
    isSupportButtonVisible: Boolean,
    viewModel: RootViewModel,
    onBackPressed: () -> Unit,
    onGoSupport: () -> Unit,
    onWebUrl: (String) -> Unit
) {
    val state by viewModel.modelFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    state.clearFocus?.use { focusManager.clearFocus() }

    val onEvent = viewModel::onEvent
    val articleTitleState = remember { mutableStateOf<String?>(null) }
    val title = when (val screen = state.screen) {
        Screen.Blocks -> state.blocksState.block.title
        is Screen.Review -> stringResource(theme.strings.articleReviewTitle)
        is Screen.Article -> screen.title ?: articleTitleState.value
        else -> screen.title
    } ?: stringResource(theme.strings.sectionsTitle)

    val scrollBehavior = rememberToolbarScrollBehavior(theme)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box(
                modifier = Modifier
                    .background(color = theme.colors.rootBackground)
            ) {
                Crossfade(
                    modifier = Modifier.animateContentSize(animationSpec = remember { theme.animationSpec() },
                        finishedListener = remember {
                            { initial, target ->
                                if (target.height < initial.height) {
                                    viewModel.onEvent(RootViewModel.Event.SearchBarAnimationFinished)
                                }
                            }
                        }),
                    targetState = state.screen !is Screen.Blocks ||
                            state.blocksState.block !is RootViewModel.State.BlocksState.Block.Search,
                    animationSpec = remember { theme.animationSpec() }
                ) { visibleToolbar ->
                    when {
                        visibleToolbar -> CustomToolbar(
                            theme = theme,
                            title = title,
                            scrollBehavior = scrollBehavior,
                            onBackPressed = onBackPressed
                        )
                        else -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(theme.dimensions.toolbarBottomPadding)
                                .background(color = theme.colors.rootBackground)
                        )
                    }
                }
            }
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(color = theme.colors.rootBackground)
            ) {
                val supportButtonVisible = remember { mutableStateOf(true) }
                Content(
                    theme = theme,
                    viewModel = viewModel,
                    state = state,
                    articleTitleState = articleTitleState,
                    supportButtonVisible = supportButtonVisible,
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
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Content(
    theme: UsedeskKnowledgeBaseTheme,
    viewModel: RootViewModel,
    state: RootViewModel.State,
    articleTitleState: MutableState<String?>,
    supportButtonVisible: MutableState<Boolean>,
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
                        supportButtonVisible = supportButtonVisible,
                        onEvent = onEvent
                    )
                    is Screen.Article -> ContentArticle(
                        theme = theme,
                        getCurrentScreen = getCurrentScreen,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        articleId = screen.articleId,
                        articleTitleState = articleTitleState,
                        supportButtonVisible = supportButtonVisible,
                        onWebUrl = onWebUrl,
                        onReview = remember { { onEvent(RootViewModel.Event.GoReview(screen.articleId)) } }
                    )
                    is Screen.Review -> ContentReview(
                        theme = theme,
                        getCurrentScreen = getCurrentScreen,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        articleId = screen.articleId,
                        supportButtonVisible = supportButtonVisible,
                        goBack = viewModel::onBackPressed
                    )
                }
            }
        }
    }
}