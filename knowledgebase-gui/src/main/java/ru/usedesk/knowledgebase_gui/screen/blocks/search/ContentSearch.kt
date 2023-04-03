package ru.usedesk.knowledgebase_gui.screen.blocks.search

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization
import ru.usedesk.knowledgebase_gui.screen.blocks.SEARCH_KEY
import ru.usedesk.knowledgebase_gui.screen.blocks.search.SearchViewModel.State.NextPageState
import ru.usedesk.knowledgebase_gui.screen.isSupportButtonVisible
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

@Preview
@Composable
private fun Preview() {
    val customization = UsedeskKnowledgeBaseCustomization()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(customization.colorIdWhite2))
    ) {
        ContentSearch(
            customization = customization,
            viewModelStoreOwner = remember { { ViewModelStore() } },
            supportButtonVisible = remember { mutableStateOf(false) },
            onArticleClick = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ContentSearch(
    customization: UsedeskKnowledgeBaseCustomization,
    viewModelStoreOwner: ViewModelStoreOwner,
    supportButtonVisible: MutableState<Boolean>,
    onArticleClick: (UsedeskArticleContent) -> Unit
) {
    val viewModel = kbUiViewModel(
        key = SEARCH_KEY,
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> SearchViewModel(kbUiComponent.interactor) }
    val state by viewModel.modelFlow.collectAsState()
    supportButtonVisible.value = state.lazyListState.isSupportButtonVisible()
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state.reloadError) { reloadError ->
            when {
                reloadError -> ScreenNotLoaded(
                    customization = customization,
                    tryAgain = if (!state.reloadLoading) viewModel::tryLoadAgain else null
                )
                else -> Box(modifier = Modifier.fillMaxSize()) {
                    val content = state.content
                    if (content != null) {
                        LazyColumn(
                            modifier = Modifier,
                            state = state.lazyListState
                        ) {
                            items(
                                items = content,
                                key = { it.item.id }
                            ) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                        .cardItem(
                                            customization = customization,
                                            isTop = item == state.content?.firstOrNull(),
                                            isBottom = item == state.content?.lastOrNull()
                                        )
                                        .clickableItem(
                                            onClick = remember { { onArticleClick(item.item) } }
                                        )
                                        .padding(
                                            start = 20.dp,
                                            end = 10.dp,
                                            top = 8.dp,
                                            bottom = 8.dp
                                        )
                                        .animateItemPlacement()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(end = 10.dp)
                                            .weight(weight = 1f, fill = true)
                                    ) {
                                        BasicText(
                                            style = customization.textStyleSearchItemTitle(),
                                            text = item.item.title
                                        )
                                        BasicText(
                                            style = customization.textStyleSearchItemDescription(),
                                            text = item.description,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        BasicText(
                                            style = customization.textStyleSearchItemPath(),
                                            text = remember(item) {
                                                "${item.sectionName} > ${item.categoryName}"
                                            }
                                        )
                                    }
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(
                                                top = 16.dp,
                                                bottom = 16.dp
                                            )
                                            .size(24.dp),
                                        painter = painterResource(customization.iconIdListItemArrowForward),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                }
                            }
                            item(key = content.size) {
                                viewModel.lowestItemShowed()
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    CardCircleProgress(
                                        customization = customization,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                bottom = 16.dp
                                            ),
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
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            BasicText(
                                modifier = Modifier.padding(16.dp),
                                text = stringResource(customization.textIdSearchIsEmpty),
                                style = customization.textStyleSearchIsEmpty()
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            CardCircleProgress(
                customization = customization,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                loading = state.reloadLoading,
                onErrorClicked = null
            )
        }
    }
}