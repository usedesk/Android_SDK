package ru.usedesk.knowledgebase_gui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.knowledgebase_gui._di.KbUiComponent
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screen.article.ARTICLE_KEY
import ru.usedesk.knowledgebase_gui.screen.article.ContentArticle
import ru.usedesk.knowledgebase_gui.screen.blocks.ContentBlocks
import ru.usedesk.knowledgebase_gui.screen.loading.ContentLoading
import ru.usedesk.knowledgebase_gui.screen.loading.LOADING_KEY
import ru.usedesk.knowledgebase_gui.screen.review.ContentReview
import ru.usedesk.knowledgebase_gui.screen.review.REVIEW_KEY
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

class UsedeskKnowledgeBaseScreen : UsedeskFragment() {
    private val viewModel: RootViewModel by viewModels(
        factoryProducer = {
            val configuration =
                argsGetParcelable<UsedeskKnowledgeBaseConfiguration>(KNOWLEDGE_BASE_CONFIGURATION)
                    ?: throw RuntimeException("UsedeskKnowledgeBaseConfiguration not found. Call the newInstance or createBundle method and put the configuration inside")
            KbUiComponent.open(requireContext(), configuration)
            KbUiViewModelFactory { RootViewModel(it.interactor) }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            val customization = remember { UsedeskKnowledgeBaseCustomization.provider() }
            ScreenRoot(customization)
        }
    }

    @Composable
    private fun ScreenRoot(customization: UsedeskKnowledgeBaseCustomization) {
        val state by viewModel.modelFlow.collectAsState()

        val focusManager = LocalFocusManager.current
        state.clearFocus?.use { focusManager.clearFocus() }

        val onEvent = viewModel::onEvent
        val title = when (val screen = state.screen) {
            is State.Screen.Loading -> null
            State.Screen.Blocks -> state.blocksState.block.title
            is State.Screen.Article -> screen.title
            is State.Screen.Review -> stringResource(customization.textIdArticleReviewTitle)
        } ?: stringResource(customization.textIdSectionsTitle)

        val scrollBehavior = rememberToolbarScrollBehavior()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(customization.colorIdWhite2))
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val supportButtonVisible = remember { mutableStateOf(true) }
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = state.blocksState.block !is State.BlocksState.Block.Search
            ) { visibleToolbar ->
                when {
                    visibleToolbar -> CustomToolbar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(customization.colorIdWhite2)),
                        customization = customization,
                        title = title,
                        scrollBehavior = scrollBehavior,
                        onBackPressed = requireActivity()::onBackPressed
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
                    customization = customization,
                    state = state,
                    supportButtonVisible = supportButtonVisible,
                    onEvent = onEvent
                )
                CardCircleChat(customization = customization,
                    visible = supportButtonVisible.value,
                    onClicked = remember {
                        { findParent<IUsedeskOnSupportClickListener>()?.onSupportClick() }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun Content(
        customization: UsedeskKnowledgeBaseCustomization,
        state: State,
        supportButtonVisible: MutableState<Boolean>,
        onEvent: (Event) -> Unit
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
                    State.Transition.FORWARD -> forwardTransitionSpec
                    State.Transition.BACKWARD -> backwardTransitionSpec
                    else -> noneTransitionSpec
                }
            }) { screen ->
            when (screen) {
                is State.Screen.Loading -> ContentLoading(
                    customization = customization,
                    viewModelStoreFactory = viewModel.viewModelStoreFactory,
                    tryAgain = remember { { onEvent(Event.TryAgain) } }
                )
                is State.Screen.Blocks -> {
                    LaunchedEffect(Unit) {
                        viewModel.viewModelStoreFactory.clear(LOADING_KEY)
                        viewModel.viewModelStoreFactory.clear(ARTICLE_KEY)
                    }
                    ContentBlocks(
                        customization = customization,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        state = state.blocksState,
                        supportButtonVisible = supportButtonVisible,
                        onEvent = onEvent
                    )
                }
                is State.Screen.Article -> {
                    LaunchedEffect(Unit) {
                        viewModel.viewModelStoreFactory.clear(REVIEW_KEY)
                    }
                    ContentArticle(
                        customization = customization,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        articleId = screen.articleId,
                        supportButtonVisible = supportButtonVisible,
                        onWebUrl = remember { { findParent<IUsedeskOnWebUrlListener>()?.onWebUrl(it) } },
                        onReview = remember { { onEvent(Event.GoReview(screen.articleId)) } }
                    )
                }
                is State.Screen.Review -> {
                    supportButtonVisible.value = false
                    ContentReview(
                        customization = customization,
                        viewModelStoreFactory = viewModel.viewModelStoreFactory,
                        articleId = screen.articleId,
                        goBack = viewModel::onBackPressed
                    )
                }
            }
        }
    }

    override fun onBackPressed() = viewModel.onBackPressed()

    companion object {
        private const val WITH_SUPPORT_BUTTON_KEY = "a"
        private const val WITH_ARTICLE_RATING_KEY = "b"
        private const val KNOWLEDGE_BASE_CONFIGURATION = "c"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(
                withSupportButton,
                withArticleRating,
                knowledgeBaseConfiguration
            )
        }

        @JvmStatic
        @JvmOverloads
        fun createBundle(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): Bundle = Bundle().apply {
            putBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
            putBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
            putParcelable(KNOWLEDGE_BASE_CONFIGURATION, knowledgeBaseConfiguration)
        }
    }
}

@Composable
internal fun LazyListState.isSupportButtonVisible() = remember(this) {
    derivedStateOf {
        firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
    }
}.value