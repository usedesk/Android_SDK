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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui._di.KbUiComponent
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.KbUiViewModelFactory
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screen.article.ARTICLE_KEY
import ru.usedesk.knowledgebase_gui.screen.article.ContentArticle
import ru.usedesk.knowledgebase_gui.screen.blocks.ContentBlocks
import ru.usedesk.knowledgebase_gui.screen.loading.ContentLoading
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
            ScreenRoot()
        }
    }

    @Composable
    private fun ScreenRoot() {
        val state by viewModel.modelFlow.collectAsState()
        val onEvent = viewModel::onEvent
        val title = when (val screen = state.screen) {
            is State.Screen.Loading -> null
            State.Screen.Blocks -> state.blocksState.block.title
            is State.Screen.Article -> screen.title
            is State.Screen.Review -> stringResource(R.string.usedesk_string_rating_whats_wrong) //TODO
        } ?: stringResource(R.string.usedesk_knowledgebase)

        val scrollBehavior = rememberToolbarScrollBehavior()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.usedesk_white_2))
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            CustomToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.usedesk_white_2)),
                title = title,
                scrollBehavior = scrollBehavior,
                onBackPressed = requireActivity()::onBackPressed
            )
            Content(
                state = state,
                onEvent = onEvent
            )
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun Content(
        state: State,
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

        val viewModelStoreFactory = remember { ViewModelStoreFactory() }
        DisposableEffect(Unit) {
            onDispose {
                viewModelStoreFactory.clearAll()
            }
        }
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
                    screen = screen,
                    onEvent = onEvent
                )
                is State.Screen.Blocks -> {
                    LaunchedEffect(Unit) {
                        viewModelStoreFactory.clear(ARTICLE_KEY)
                    }
                    ContentBlocks(
                        viewModelStoreFactory = viewModelStoreFactory,
                        state = state.blocksState,
                        onEvent = onEvent
                    )
                }
                is State.Screen.Article -> {
                    LaunchedEffect(Unit) {
                        viewModelStoreFactory.clear(REVIEW_KEY)
                    }
                    ContentArticle(
                        viewModelStoreFactory = viewModelStoreFactory,
                        articleId = screen.articleId,
                        onWebUrl = remember {
                            { findParent<IUsedeskOnWebUrlListener>()?.onWebUrl(it) }
                        },
                        onReview = remember {
                            { onEvent(Event.GoReview(screen.articleId)) }
                        }
                    )
                }
                is State.Screen.Review -> ContentReview(
                    viewModelStoreFactory = viewModelStoreFactory,
                    articleId = screen.articleId,
                    goBack = viewModel::onBackPressed
                )
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