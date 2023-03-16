package ru.usedesk.knowledgebase_gui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel.State
import ru.usedesk.knowledgebase_gui.screens.main.compose.*
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

class UsedeskKnowledgeBaseScreen : UsedeskFragment() {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = (inflater.inflate(
        R.layout.usedesk_compose_screen,
        container,
        false
    ) as ComposeView).apply {
        val configuration =
            argsGetParcelable<UsedeskKnowledgeBaseConfiguration>(KNOWLEDGE_BASE_CONFIGURATION)
        UsedeskKnowledgeBaseSdk.init(requireContext(), configuration)
        setContent {
            val state by viewModel.modelFlow.collectAsState()
            ScreenRoot(state, viewModel::onEvent)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ScreenRoot(
        state: State,
        onEvent: (Event) -> Unit
    ) {
        val sectionsTitle = when (state.currentScreen) {
            is State.Screen.Loading,
            is State.Screen.Sections -> stringResource(R.string.usedesk_knowledgebase)
            is State.Screen.Categories -> state.currentScreen.section.title
            is State.Screen.Articles -> state.currentScreen.category.title
            is State.Screen.Article -> state.currentScreen.article.title
        }
        val title = remember(state.currentScreen) {
            when (state.currentScreen) {
                else -> sectionsTitle
            }
        }

        val scrollBehavior = rememberToolbarScrollBehavior()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.usedesk_white_2))
                ) {
                    CustomToolbar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.usedesk_white_2)),
                        title = title,
                        scrollBehavior = scrollBehavior,
                        onBackPressed = requireActivity()::onBackPressed
                    )
                    SearchBar(
                        state = state,
                        onEvent = onEvent
                    )
                }
            },
            content = {
                Content(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(colorResource(R.color.usedesk_white_2))
                )
            }
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun Content(
        state: State,
        onEvent: (Event) -> Unit,
        modifier: Modifier
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
        val replaceTransitionSpec = fadeIn() with fadeOut()
        val noneTransitionSpec = EnterTransition.None with ExitTransition.None
        Box(modifier = modifier) {
            AnimatedContent(
                targetState = state.currentScreen,
                transitionSpec = {
                    when (targetState.transition(initialState)) {
                        State.Screen.Transition.FORWARD -> forwardTransitionSpec
                        State.Screen.Transition.BACKWARD -> backwardTransitionSpec
                        State.Screen.Transition.REPLACE -> replaceTransitionSpec
                        else -> noneTransitionSpec
                    }
                }
            ) { currentScreen ->
                when (currentScreen) {
                    is State.Screen.Loading -> ContentLoading(
                        screen = currentScreen,
                        onEvent = onEvent
                    )
                    is State.Screen.Sections -> ContentSections(
                        screen = currentScreen,
                        onEvent = onEvent
                    )
                    is State.Screen.Categories -> ContentCategories(
                        screen = currentScreen,
                        onEvent = onEvent
                    )
                    is State.Screen.Articles -> ContentArticles(
                        screen = currentScreen,
                        onEvent = onEvent
                    )
                    is State.Screen.Article -> ContentArticle(
                        screen = currentScreen,
                        onEvent = onEvent
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentArticle(
        screen: State.Screen.Article,
        onEvent: (Event) -> Unit
    ) {
        LazyColumnCard { //TODO: тут поиск не нужен
            items(100) {
                BasicText(text = "Article:${screen.article.title}")
            }
        }
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed()

    companion object {
        private const val WITH_SUPPORT_BUTTON_KEY = "withSupportButtonKey"
        private const val WITH_ARTICLE_RATING_KEY = "withArticleRatingKey"
        private const val KNOWLEDGE_BASE_CONFIGURATION = "knowledgeBaseConfiguration"

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
        fun createBundle( //TODO
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