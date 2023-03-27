package ru.usedesk.knowledgebase_gui.screen.blocks.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.cardItem
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State.BlocksState
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.usedesk_white_2))
    ) {
        ContentArticles(
            viewModelStoreOwner = remember { { ViewModelStore() } },
            block = BlocksState.Block.Articles(
                BlocksState.Block.Sections(),
                "Title",
                1L
            ),
            onArticleClick = {}
        )
    }
}

@Composable
internal fun ContentArticles(
    viewModelStoreOwner: ViewModelStoreOwner,
    block: BlocksState.Block.Articles,
    onArticleClick: (UsedeskArticleInfo) -> Unit
) {
    val viewModel =
        kbUiViewModel(
            key = block.categoryId.toString(),
            viewModelStoreOwner = viewModelStoreOwner
        ) { kbUiComponent -> ArticlesViewModel(kbUiComponent.interactor, block.categoryId) }
    val state by viewModel.modelFlow.collectAsState()
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
            key = UsedeskArticleInfo::id
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
}