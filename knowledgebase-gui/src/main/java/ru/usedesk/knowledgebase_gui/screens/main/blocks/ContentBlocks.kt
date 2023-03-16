package ru.usedesk.knowledgebase_gui.screens.main.blocks

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ru.usedesk.knowledgebase_gui.compose.SearchBar
import ru.usedesk.knowledgebase_gui.compose.composeViewModel
import ru.usedesk.knowledgebase_gui.screens.main.blocks.BlocksViewModel.State
import ru.usedesk.knowledgebase_gui.screens.main.blocks.articles.ContentArticles
import ru.usedesk.knowledgebase_gui.screens.main.blocks.categories.ContentCategories
import ru.usedesk.knowledgebase_gui.screens.main.blocks.sections.ContentSections
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ContentBlocks(
    onArticleClick: (UsedeskArticleInfo) -> Unit
) {
    val viewModel: BlocksViewModel = composeViewModel { usedeskKb ->
        BlocksViewModel(usedeskKb)
    }
    val state by viewModel.modelFlow.collectAsState()

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

    Column(modifier = Modifier) {
        SearchBar(
            value = state.searchText,
            onValueChange = viewModel::onSearchValue
        )
        AnimatedContent(
            targetState = state.page,
            transitionSpec = {
                when (targetState.transition(initialState)) {
                    State.Page.Transition.FORWARD -> forwardTransitionSpec
                    State.Page.Transition.BACKWARD -> backwardTransitionSpec
                    State.Page.Transition.STAY -> replaceTransitionSpec
                    else -> noneTransitionSpec
                }
            }
        ) { currentScreen ->
            when (currentScreen) {
                is State.Page.Sections -> ContentSections(viewModel::onSectionClick)
                is State.Page.Categories -> ContentCategories(
                    currentScreen.sectionId,
                    viewModel::onCategoryClick
                )
                is State.Page.Articles -> ContentArticles(
                    currentScreen.categoryId,
                    onArticleClick
                )
            }
        }
    }
}