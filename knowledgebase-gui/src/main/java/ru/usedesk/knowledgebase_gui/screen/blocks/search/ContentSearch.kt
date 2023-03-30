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
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State.BlocksState
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization
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
            block = BlocksState.Block.Search(
                BlocksState.Block.Sections()
            ),
            onArticleClick = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ContentSearch(
    customization: UsedeskKnowledgeBaseCustomization,
    viewModelStoreOwner: ViewModelStoreOwner,
    block: BlocksState.Block.Search,
    onArticleClick: (UsedeskArticleContent) -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> SearchViewModel(kbUiComponent.interactor) }
    val state by viewModel.modelFlow.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state.contentState) { contentState ->
            when (contentState) {
                is ContentState.Empty -> Box(modifier = Modifier.fillMaxSize())
                is ContentState.Error -> ScreenNotLoaded(
                    customization = customization,
                    tryAgain = if (!state.loading) viewModel::tryAgain else null
                )
                is ContentState.Loaded -> Box(modifier = Modifier.fillMaxSize()) {
                    val lowestItem = remember(contentState.content) {
                        state.content.takeLast(5).firstOrNull()
                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp
                            ),
                        state = viewModel.lazyListState //TODO: посмотреть ещё отображение краёв скролла, оно разное
                    ) {
                        items(
                            items = state.content,
                            key = { it.item.id }
                        ) { item ->
                            if (!state.loading && lowestItem == item) {
                                viewModel.lowestItemShowed()
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                    }

                    AnimatedVisibility(
                        state.content.isEmpty(),
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

        Box(modifier = Modifier.fillMaxWidth()) {
            CardCircleProgress(
                customization = customization,
                modifier = Modifier
                    .align(Alignment.Center),
                visible = state.loading
            )
        }
    }
}