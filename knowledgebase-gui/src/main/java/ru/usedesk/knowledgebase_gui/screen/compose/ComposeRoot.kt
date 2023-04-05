package ru.usedesk.knowledgebase_gui.screen.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.compose.CardCircleChat
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screen.RootViewModel
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.article.ARTICLE_KEY
import ru.usedesk.knowledgebase_gui.screen.compose.article.ContentArticle
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.ContentBlocks
import ru.usedesk.knowledgebase_gui.screen.compose.loading.ContentLoading
import ru.usedesk.knowledgebase_gui.screen.compose.loading.LOADING_KEY
import ru.usedesk.knowledgebase_gui.screen.compose.review.ContentReview
import ru.usedesk.knowledgebase_gui.screen.compose.review.REVIEW_KEY

@Composable
internal fun ComposeRoot(
    theme: UsedeskKnowledgeBaseTheme,
    viewModel: RootViewModel,
    onBackPressed: () -> Unit,
    onGoSupport: () -> Unit,
    onWebUrl: (String) -> Unit
) {
    val state by viewModel.modelFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    state.clearFocus?.use { focusManager.clearFocus() }

    val onEvent = viewModel::onEvent
    val title = when (val screen = state.screen) {
        is RootViewModel.State.Screen.Loading -> null
        RootViewModel.State.Screen.Blocks -> state.blocksState.block.title
        is RootViewModel.State.Screen.Article -> screen.title
        is RootViewModel.State.Screen.Review -> stringResource(theme.strings.articleReviewTitle)
    } ?: stringResource(theme.strings.sectionsTitle)

    val scrollBehavior = rememberToolbarScrollBehavior()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        val supportButtonVisible = remember { mutableStateOf(true) }
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = state.blocksState.block !is RootViewModel.State.BlocksState.Block.Search
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
                        .height(16.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Content(
                theme = theme,
                viewModel = viewModel,
                state = state,
                supportButtonVisible = supportButtonVisible,
                onWebUrl = onWebUrl,
                onEvent = onEvent
            )
            CardCircleChat(
                theme = theme,
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
    supportButtonVisible: MutableState<Boolean>,
    onWebUrl: (String) -> Unit,
    onEvent: (RootViewModel.Event) -> Unit
) {
    val forwardTransitionSpec = remember {
        slideInHorizontally(
            spring(
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            )
        ) { it } with slideOutHorizontally(
            spring(
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            )
        ) { -it }
    }
    val backwardTransitionSpec = slideInHorizontally(
        spring(
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    ) { -it } with slideOutHorizontally(
        spring(
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    ) { it }
    val noneTransitionSpec = fadeIn() with fadeOut()

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
        when (screen) {
            is RootViewModel.State.Screen.Loading -> {
                DisposableEffect(Unit) {
                    onDispose {
                        viewModel.viewModelStoreFactory.clear(LOADING_KEY)
                    }
                }
                ContentLoading(
                    theme = theme,
                    viewModelStoreFactory = viewModel.viewModelStoreFactory,
                    tryAgain = remember { { onEvent(RootViewModel.Event.TryAgain) } }
                )
            }
            is RootViewModel.State.Screen.Blocks -> {
                ContentBlocks(
                    theme = theme,
                    viewModelStoreFactory = viewModel.viewModelStoreFactory,
                    viewModel = viewModel,
                    supportButtonVisible = supportButtonVisible,
                    onEvent = onEvent
                )
            }
            is RootViewModel.State.Screen.Article -> {
                DisposableEffect(Unit) {
                    onDispose {
                        when (viewModel.modelFlow.value.screen) {
                            RootViewModel.State.Screen.Blocks,
                            RootViewModel.State.Screen.Loading ->
                                viewModel.viewModelStoreFactory.clear(ARTICLE_KEY)
                            is RootViewModel.State.Screen.Article,
                            is RootViewModel.State.Screen.Review -> Unit
                        }
                    }
                }
                ContentArticle(
                    theme = theme,
                    viewModelStoreFactory = viewModel.viewModelStoreFactory,
                    articleId = screen.articleId,
                    supportButtonVisible = supportButtonVisible,
                    onWebUrl = onWebUrl,
                    onReview = remember { { onEvent(RootViewModel.Event.GoReview(screen.articleId)) } }
                )
            }
            is RootViewModel.State.Screen.Review -> {
                supportButtonVisible.value = false
                DisposableEffect(Unit) {
                    onDispose {
                        viewModel.viewModelStoreFactory.clear(REVIEW_KEY)
                    }
                }
                ContentReview(
                    theme = theme,
                    viewModelStoreFactory = viewModel.viewModelStoreFactory,
                    articleId = screen.articleId,
                    goBack = viewModel::onBackPressed
                )
            }
        }
    }
}