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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization
import ru.usedesk.knowledgebase_gui.screen.blocks.SEARCH_KEY
import ru.usedesk.knowledgebase_gui.screen.blocks.search.SearchViewModel.State.NextPageState
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
            onArticleClick = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ContentSearch(
    customization: UsedeskKnowledgeBaseCustomization,
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClick: (UsedeskArticleContent) -> Unit
) {
    val viewModel = kbUiViewModel(
        key = SEARCH_KEY,
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> SearchViewModel(kbUiComponent.interactor) }
    UsedeskLog.onLog("ContentSearch") { viewModel.toString() }
    val state by viewModel.modelFlow.collectAsState()
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
                                            isTop = item.first,
                                            isBottom = item.last
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
                                    BasicText(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(end = 10.dp)
                                            .weight(weight = 1f, fill = true),
                                        style = TextStyle(
                                            fontSize = 17.sp,
                                            textAlign = TextAlign.Start,
                                            color = colorResource(customization.colorIdBlack2)
                                        ),
                                        text = item.item.title
                                    )
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(
                                                top = 16.dp,
                                                bottom = 16.dp
                                            )
                                            .size(24.dp),
                                        painter = painterResource(R.drawable.usedesk_ic_arrow_forward),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                }
                            }
                            item {
                                val state by viewModel.modelFlow.collectAsState()
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
                            content.isEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            BasicText(
                                modifier = Modifier.padding(16.dp),
                                text = stringResource(customization.textIdSearchIsEmpty)
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