package ru.usedesk.knowledgebase_gui.screen.blocks.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.cardItem
import ru.usedesk.knowledgebase_gui.compose.clickableArea
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State.BlocksState
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.usedesk_white_2))
    ) {
        ContentSearch(
            viewModelStoreOwner = remember { { ViewModelStore() } },
            block = BlocksState.Block.Search(
                BlocksState.Block.Sections()
            ),
            onArticleClick = {}
        )
    }
}

@Composable
internal fun ScreenNotLoaded(
    loading: Boolean,
    tryAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                painter = painterResource(R.drawable.usedesk_image_cant_load),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    start = 24.dp,
                    end = 24.dp
                )
        ) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                text = "Ne udalos' zagruzit'", //TODO
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = colorResource(R.color.usedesk_black_2),
                    textAlign = TextAlign.Center
                )
            )
            Crossfade(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                targetState = loading
            ) { loading ->
                when {
                    loading -> Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            color = colorResource(R.color.usedesk_red),
                            strokeWidth = 3.dp
                        )
                    }
                    else -> BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableArea(
                                radius = 30.dp,
                                onClick = tryAgain
                            )
                            .align(Alignment.CenterHorizontally),
                        text = "Трай агаин", //TODO
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp,
                            color = colorResource(R.color.usedesk_blue),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun ContentSearch(
    viewModelStoreOwner: ViewModelStoreOwner,
    block: BlocksState.Block.Search,
    onArticleClick: (UsedeskArticleContent) -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> SearchViewModel(kbUiComponent.interactor) }
    val state by viewModel.modelFlow.collectAsState()
    Crossfade(targetState = state.error) { error ->
        when {
            error -> ScreenNotLoaded(
                state.loading,
                viewModel::tryAgain
            )
            else -> Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp
                        ),
                    state = block.lazyListState
                ) {
                    items(
                        items = state.articles,
                        key = UsedeskArticleContent::id
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cardItem(
                                    isTop = it == state.articles.firstOrNull(),
                                    isBottom = it == state.articles.lastOrNull()
                                )
                                .clickableItem(
                                    onClick = remember { { onArticleClick(it) } }
                                )
                                .padding(
                                    start = 20.dp,
                                    end = 10.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                )
                        ) {
                            BasicText(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(end = 10.dp)
                                    .weight(weight = 1f, fill = true),
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Start,
                                    color = colorResource(R.color.usedesk_black_2)
                                ),
                                text = it.title
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
                    state.empty,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    BasicText(
                        modifier = Modifier.padding(16.dp),
                        text = "Nichego ne naideno" //TODO
                    )
                }

                AnimatedVisibility(
                    state.loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(colorResource(R.color.usedesk_white_1))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}