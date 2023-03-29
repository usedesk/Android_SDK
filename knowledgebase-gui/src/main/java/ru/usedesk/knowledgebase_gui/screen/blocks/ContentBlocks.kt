package ru.usedesk.knowledgebase_gui.screen.blocks

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ru.usedesk.knowledgebase_gui.compose.SearchBar
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization
import ru.usedesk.knowledgebase_gui.screen.blocks.articles.ContentArticles
import ru.usedesk.knowledgebase_gui.screen.blocks.categories.ContentCategories
import ru.usedesk.knowledgebase_gui.screen.blocks.search.ContentSearch
import ru.usedesk.knowledgebase_gui.screen.blocks.sections.ContentSections

internal const val SECTIONS_KEY = "sections"
internal const val CATEGORIES_KEY = "categories"
internal const val ARTICLES_KEY = "articles"
internal const val SEARCH_KEY = "search"

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ContentBlocks(
    customization: UsedeskKnowledgeBaseCustomization,
    viewModelStoreFactory: ViewModelStoreFactory,
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
    val noneTransitionSpec = fadeIn() with fadeOut()

    Column(modifier = Modifier) {
        SearchBar(
            customization = customization,
            value = state.searchText,
            onClearClick = remember { { onEvent(Event.SearchClearClicked) } },
            onCancelClick = when (state.block) {
                is State.BlocksState.Block.Search -> remember { { onEvent(Event.SearchCancelClicked) } }
                else -> null
            },
            onValueChange = remember { { onEvent(Event.SearchTextChanged(it)) } },
            onSearch = remember { { onEvent(Event.SearchClicked) } }
        )
        AnimatedContent(
            targetState = state.block,
            transitionSpec = {
                when (targetState.transition(initialState)) {
                    State.Transition.FORWARD -> forwardTransitionSpec
                    State.Transition.BACKWARD -> backwardTransitionSpec
                    else -> noneTransitionSpec
                }
            }
        ) { block ->
            when (block) {
                is State.BlocksState.Block.Sections -> {
                    LaunchedEffect(Unit) {
                        viewModelStoreFactory.clear(CATEGORIES_KEY)
                    }
                    ContentSections(
                        customization = customization,
                        viewModelStoreOwner = remember {
                            { viewModelStoreFactory.get(SECTIONS_KEY) }
                        },
                        block = block,
                        onSectionClicked = remember { { onEvent(Event.SectionClicked(it)) } }
                    )
                }
                is State.BlocksState.Block.Categories -> {
                    LaunchedEffect(Unit) {
                        viewModelStoreFactory.clear(ARTICLES_KEY)
                    }
                    ContentCategories(
                        customization = customization,
                        viewModelStoreOwner = remember {
                            { viewModelStoreFactory.get(CATEGORIES_KEY) }
                        },
                        block = block,
                        onCategoryClick = remember { { onEvent(Event.CategoryClicked(it)) } }
                    )
                }
                is State.BlocksState.Block.Articles -> ContentArticles(
                    customization = customization,
                    viewModelStoreOwner = remember { { viewModelStoreFactory.get(ARTICLES_KEY) } },
                    block = block,
                    onArticleClick = remember { { onEvent(Event.ArticleClicked(it.id, it.title)) } }
                )
                is State.BlocksState.Block.Search -> ContentSearch(
                    customization = customization,
                    viewModelStoreOwner = remember { { viewModelStoreFactory.get(SEARCH_KEY) } },
                    block = block,
                    onArticleClick = remember { { onEvent(Event.ArticleClicked(it.id, it.title)) } }
                )
            }
        }
    }
}