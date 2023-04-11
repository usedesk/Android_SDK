package ru.usedesk.knowledgebase_gui.screen.compose.blocks

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.screen.RootViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.SearchBar
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.articles.ContentArticles
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.categories.ContentCategories
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.search.ContentSearch
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.sections.ContentSections

internal const val SECTIONS_KEY = "sections"
internal const val CATEGORIES_KEY = "categories"
internal const val ARTICLES_KEY = "articles"
internal const val SEARCH_KEY = "search"

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ContentBlocks(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreFactory: ViewModelStoreFactory,
    viewModel: RootViewModel,
    supportButtonVisible: MutableState<Boolean>,
    onEvent: (Event) -> Unit
) {
    val state by viewModel.modelFlow.collectAsState()
    val blocksState = state.blocksState

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

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            theme = theme,
            value = blocksState.searchText,
            focused = state.blocksState.searchBarFocused,
            onClearClick = remember { { onEvent(Event.SearchClearClicked) } },
            onCancelClick = when (blocksState.block) {
                is State.BlocksState.Block.Search -> remember { { onEvent(Event.SearchCancelClicked) } }
                else -> null
            },
            onValueChange = remember { { onEvent(Event.SearchTextChanged(it)) } },
            onSearchBarClicked = remember { { onEvent(Event.SearchBarClicked) } },
            onSearch = remember { { onEvent(Event.SearchClicked) } }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = blocksState.block,
                transitionSpec = {
                    when (targetState.transition(initialState)) {
                        State.Transition.FORWARD -> forwardTransitionSpec
                        State.Transition.BACKWARD -> backwardTransitionSpec
                        else -> noneTransitionSpec
                    }
                }
            ) { block ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (block) {
                        State.BlocksState.Block.Sections -> {
                            ContentSections(
                                theme = theme,
                                viewModelStoreOwner = remember {
                                    { viewModelStoreFactory.get(SECTIONS_KEY) }
                                },
                                supportButtonVisible = supportButtonVisible,
                                onSectionClicked = remember { { onEvent(Event.SectionClicked(it)) } }
                            )
                        }
                        is State.BlocksState.Block.Categories -> {
                            DisposableEffect(Unit) {
                                onDispose {
                                    when (viewModel.modelFlow.value.blocksState.block) {
                                        is State.BlocksState.Block.Articles,
                                        is State.BlocksState.Block.Categories,
                                        is State.BlocksState.Block.Search -> Unit
                                        State.BlocksState.Block.Sections ->
                                            viewModelStoreFactory.clear(CATEGORIES_KEY)
                                    }
                                }
                            }
                            ContentCategories(
                                theme = theme,
                                viewModelStoreOwner = remember {
                                    { viewModelStoreFactory.get(CATEGORIES_KEY) }
                                },
                                sectionId = block.sectionId,
                                supportButtonVisible = supportButtonVisible,
                                onCategoryClick = remember { { onEvent(Event.CategoryClicked(it)) } }
                            )
                        }
                        is State.BlocksState.Block.Articles -> {
                            DisposableEffect(Unit) {
                                onDispose {
                                    when (viewModel.modelFlow.value.blocksState.block) {
                                        is State.BlocksState.Block.Articles,
                                        is State.BlocksState.Block.Search -> Unit
                                        is State.BlocksState.Block.Categories,
                                        State.BlocksState.Block.Sections ->
                                            viewModelStoreFactory.clear(ARTICLES_KEY)
                                    }
                                }
                            }
                            ContentArticles(
                                theme = theme,
                                viewModelStoreOwner = remember {
                                    {
                                        viewModelStoreFactory.get(
                                            ARTICLES_KEY
                                        )
                                    }
                                },
                                categoryId = block.categoryId,
                                supportButtonVisible = supportButtonVisible,
                                onArticleClick = remember {
                                    { onEvent(Event.ArticleClicked(it.id, it.title)) }
                                }
                            )
                        }
                        is State.BlocksState.Block.Search -> {
                            DisposableEffect(Unit) {
                                onDispose {
                                    when (viewModel.modelFlow.value.blocksState.block) {
                                        is State.BlocksState.Block.Search -> Unit
                                        is State.BlocksState.Block.Articles,
                                        is State.BlocksState.Block.Categories,
                                        State.BlocksState.Block.Sections ->
                                            viewModelStoreFactory.clear(SEARCH_KEY)
                                    }
                                }
                            }
                            ContentSearch(
                                theme = theme,
                                viewModelStoreOwner = remember {
                                    {
                                        viewModelStoreFactory.get(
                                            SEARCH_KEY
                                        )
                                    }
                                },
                                supportButtonVisible = supportButtonVisible,
                                onArticleClick = remember {
                                    {
                                        onEvent(
                                            Event.ArticleClicked(
                                                it.id,
                                                it.title
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}