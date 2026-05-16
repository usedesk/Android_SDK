package ru.usedesk.knowledgebase_gui.screen.compose.blocks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui.compose.StoreKeys
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.compose.rememberViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.screen.RootViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.SearchBar
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.articles.ContentArticles
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.categories.ContentCategories
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.search.ContentSearch
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.sections.ContentSections

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ContentBlocks(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreFactory: ViewModelStoreFactory,
    viewModel: RootViewModel,
    onSupportButtonVisibleChange: (Boolean) -> Unit,
    onEvent: (Event) -> Unit
) {
    val state = viewModel.modelFlow.collectAsState()
    // derivedStateOf scopes the snapshot subscription to *this* derived value, so this
    // composable invalidates only when the specific field changes — typing in the search bar
    // updates only searchText (read via the lambda below inside SearchBar) and does not
    // re-run ContentBlocks.
    val block by remember { derivedStateOf { state.value.blocksState.block } }
    val focused by remember { derivedStateOf { state.value.blocksState.searchBarFocused } }
    val searchTextProvider = remember { { state.value.blocksState.searchText } }

    val forwardTransitionSpec = remember {
        slideInHorizontally(theme.animationSpec()) { it } togetherWith
                slideOutHorizontally(theme.animationSpec()) { -it }
    }
    val backwardTransitionSpec = remember {
        slideInHorizontally(theme.animationSpec()) { -it } togetherWith
                slideOutHorizontally(theme.animationSpec()) { it }
    }
    val noneTransitionSpec = remember {
        fadeIn(theme.animationSpec()) togetherWith
                fadeOut(theme.animationSpec())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SearchBar(
            theme = theme,
            value = searchTextProvider,
            focused = focused,
            onClearClick = remember { { onEvent(Event.SearchClearClicked) } },
            onCancelClick = when (block) {
                is State.BlocksState.Block.Search -> remember { { onEvent(Event.SearchCancelClicked) } }
                else -> null
            },
            onValueChange = remember { { onEvent(Event.SearchTextChange(it)) } },
            onSearchBarClicked = remember { { onEvent(Event.SearchBarClicked) } },
            onSearch = remember { { onEvent(Event.SearchClicked) } }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = block,
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
                                viewModelStoreOwner = rememberViewModelStoreOwner {
                                    viewModelStoreFactory.get(StoreKeys.SECTIONS.name)
                                },
                                onSupportButtonVisibleChange = onSupportButtonVisibleChange,
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
                                            viewModelStoreFactory.clear(StoreKeys.CATEGORIES.name)
                                    }
                                }
                            }
                            ContentCategories(
                                theme = theme,
                                viewModelStoreOwner = rememberViewModelStoreOwner {
                                    viewModelStoreFactory.get(StoreKeys.CATEGORIES.name)
                                },
                                sectionId = block.sectionId,
                                onSupportButtonVisibleChange = onSupportButtonVisibleChange,
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
                                            viewModelStoreFactory.clear(StoreKeys.ARTICLES.name)
                                    }
                                }
                            }
                            ContentArticles(
                                theme = theme,
                                viewModelStoreOwner = rememberViewModelStoreOwner {
                                    viewModelStoreFactory.get(StoreKeys.ARTICLES.name)
                                },
                                categoryId = block.categoryId,
                                onSupportButtonVisibleChange = onSupportButtonVisibleChange,
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
                                            viewModelStoreFactory.clear(StoreKeys.SEARCH.name)
                                    }
                                }
                            }
                            ContentSearch(
                                theme = theme,
                                viewModelStoreOwner = rememberViewModelStoreOwner {
                                    viewModelStoreFactory.get(StoreKeys.SEARCH.name)
                                },
                                onSupportButtonVisibleChange = onSupportButtonVisibleChange,
                                onArticleClick = remember {
                                    { onEvent(Event.ArticleClicked(it.id, it.title)) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}