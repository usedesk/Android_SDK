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
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screens.main.blocks.articles.ContentArticles
import ru.usedesk.knowledgebase_gui.screens.main.blocks.categories.ContentCategories
import ru.usedesk.knowledgebase_gui.screens.main.blocks.sections.ContentSections

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ContentBlocks(
    state: State.BlocksState,
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

    Column(modifier = Modifier) {
        SearchBar(
            value = state.searchText,
            onValueChange = remember { { onEvent(Event.SearchTextChanged(it)) } }
        )
        AnimatedContent(
            targetState = state.block,
            transitionSpec = {
                when (targetState.transition(initialState)) {
                    State.Transition.FORWARD -> forwardTransitionSpec
                    State.Transition.BACKWARD -> backwardTransitionSpec
                    State.Transition.STAY -> replaceTransitionSpec
                    else -> noneTransitionSpec
                }
            }
        ) { block ->
            when (block) {
                State.BlocksState.Block.Sections -> ContentSections(
                    onSectionClicked = remember { { onEvent(Event.SectionClicked(it)) } }
                )
                is State.BlocksState.Block.Categories -> ContentCategories(
                    sectionId = block.sectionId,
                    onCategoryClick = remember { { onEvent(Event.CategoryClicked(it)) } }
                )
                is State.BlocksState.Block.Articles -> ContentArticles(
                    categoryId = block.categoryId,
                    onArticleClick = remember { { onEvent(Event.ArticleClicked(it)) } }
                )
            }
        }
    }
}