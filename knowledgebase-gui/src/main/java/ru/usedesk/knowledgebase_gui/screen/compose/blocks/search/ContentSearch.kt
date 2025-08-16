package ru.usedesk.knowledgebase_gui.screen.compose.blocks.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.compose.CardCircleProgress
import ru.usedesk.knowledgebase_gui.compose.KbUiViewModelFactory
import ru.usedesk.knowledgebase_gui.compose.ScreenNotLoaded
import ru.usedesk.knowledgebase_gui.compose.cardItem
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.isSupportButtonVisible
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.compose.padding
import ru.usedesk.knowledgebase_gui.compose.rememberViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.screen.ComposeUtils
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.search.SearchViewModel.State.NextPageState
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

@Preview
@Composable
private fun Preview() {
    val theme = UsedeskKnowledgeBaseTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
    ) {
        ContentSearch(
            theme = theme,
            viewModelStoreOwner = rememberViewModelStoreOwner { ViewModelStore() },
            supportButtonVisible = remember { mutableStateOf(false) },
            onArticleClick = {}
        )
    }
}

@Composable
internal fun ContentSearch(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreOwner: ViewModelStoreOwner,
    supportButtonVisible: MutableState<Boolean>,
    onArticleClick: (UsedeskArticleContent) -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = KbUiViewModelFactory { kbUiComponent -> SearchViewModel(kbUiComponent.interactor) }
    )
    val state by viewModel.modelFlow.collectAsState()
    supportButtonVisible.value = state.lazyListState.isSupportButtonVisible()
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = state.reloadError,
            animationSpec = remember { theme.animationSpec() }
        ) { reloadError ->
            when {
                reloadError -> ScreenNotLoaded(
                    theme = theme,
                    tryAgain = viewModel::tryLoadAgain,
                    tryAgainVisible = !state.reloadLoading
                )

                else -> Box(modifier = Modifier.fillMaxSize()) {
                    val content = state.content
                    if (content != null) {
                        LazyColumn(
                            modifier = Modifier,
                            contentPadding = ComposeUtils.contentInsetsBottom(theme),
                            state = state.lazyListState
                        ) {
                            items(
                                items = content,
                                key = { it.item.id }
                            ) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .cardItem(
                                            theme = theme,
                                            isTop = item == state.content?.firstOrNull(),
                                            isBottom = item == state.content?.lastOrNull()
                                        )
                                        .clickableItem(
                                            onClick = remember { { onArticleClick(item.item) } }
                                        )
                                        .padding(theme.dimensions.searchItemInnerPadding)
                                        .animateItem()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .weight(weight = 1f, fill = true)
                                    ) {
                                        BasicText(
                                            modifier = Modifier.padding(theme.dimensions.searchItemTitlePadding),
                                            style = theme.textStyles.searchItemTitle,
                                            text = item.item.title
                                        )
                                        BasicText(
                                            modifier = Modifier.padding(theme.dimensions.searchItemDescriptionPadding),
                                            style = theme.textStyles.searchItemDescription,
                                            text = item.description,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        BasicText(
                                            modifier = Modifier.padding(theme.dimensions.searchItemPathPadding),
                                            style = theme.textStyles.searchItemPath,
                                            text = remember(item) {
                                                "${item.sectionName} > ${item.categoryName}"
                                            }
                                        )
                                    }
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .size(theme.dimensions.searchItemArrowSize),
                                        painter = painterResource(theme.drawables.iconListItemArrowForward),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                }
                            }
                            item(key = content.size) {
                                viewModel.lowestItemShowed()
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    CardCircleProgress(
                                        theme = theme,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(theme.dimensions.paginationLoadingPadding),
                                        loading = state.nextPageState == NextPageState.LOADING,
                                        onErrorClicked = when (state.nextPageState) {
                                            NextPageState.ERROR -> viewModel::tryNextPageAgain
                                            else -> null
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = content.isEmpty(),
                            enter = remember { fadeIn(theme.animationSpec()) },
                            exit = remember { fadeOut(theme.animationSpec()) }
                        ) {
                            BasicText(
                                modifier = Modifier.padding(theme.dimensions.searchEmptyTopPadding),
                                text = stringResource(theme.strings.searchIsEmpty),
                                style = theme.textStyles.searchIsEmpty
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            CardCircleProgress(
                theme = theme,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(theme.dimensions.loadingPadding),
                loading = state.reloadLoading,
                onErrorClicked = null
            )
        }
    }
}