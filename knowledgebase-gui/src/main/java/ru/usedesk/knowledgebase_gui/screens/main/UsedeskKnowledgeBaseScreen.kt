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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.composeViewModel
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screens.main.article.ContentArticle
import ru.usedesk.knowledgebase_gui.screens.main.blocks.ContentBlocks
import ru.usedesk.knowledgebase_gui.screens.main.loading.ContentLoading
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

class UsedeskKnowledgeBaseScreen : UsedeskFragment() {

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
                ?: throw RuntimeException("UsedeskKnowledgeBaseConfiguration not found. Call the newInstance or createBundle method and put the configuration inside")
        UsedeskKnowledgeBaseSdk.init(
            requireContext(),
            configuration
        )
        setContent {
            ScreenRoot()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ScreenRoot() {
        val viewModel: RootViewModel = composeViewModel { usedeskKb ->
            RootViewModel(usedeskKb)
        }
        val state by viewModel.modelFlow.collectAsState()
        val onEvent = viewModel::onEvent
        val sectionsTitle = stringResource(R.string.usedesk_knowledgebase)
        val title = sectionsTitle/*when (state.page) {
            is State.Page.Loading,
            is State.Page.Sections -> stringResource(R.string.usedesk_knowledgebase)
            is State.Page.Categories -> state.page.section.title
            is State.Page.Articles -> state.page.category.title
            is State.Page.Article -> state.page.article.title
        }*/ //TODO: как это вообще делать?

        val scrollBehavior = rememberToolbarScrollBehavior()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.usedesk_white_2))
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CustomToolbar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = title,
                    scrollBehavior = scrollBehavior,
                    onBackPressed = requireActivity()::onBackPressed
                )
            }
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
        val replaceTransitionSpec = fadeIn() with fadeOut()
        val noneTransitionSpec = EnterTransition.None with ExitTransition.None
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = state.screen,
            transitionSpec = {
                when (targetState.transition(initialState)) {
                    State.Transition.FORWARD -> forwardTransitionSpec
                    State.Transition.BACKWARD -> backwardTransitionSpec
                    State.Transition.STAY -> replaceTransitionSpec
                    else -> noneTransitionSpec
                }
            }) { screen ->
            when (screen) {
                is State.Screen.Article -> ContentArticle(
                    screen = screen,
                    onEvent = onEvent
                )
                is State.Screen.Blocks -> ContentBlocks(
                    screen = screen,
                    onEvent = onEvent
                )
                is State.Screen.Loading -> ContentLoading(
                    screen = screen,
                    onEvent = onEvent
                )
            }
        }
    }

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