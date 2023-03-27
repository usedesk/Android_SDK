package ru.usedesk.knowledgebase_gui.screen.blocks.categories

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
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.usedesk_white_2))
    ) {
        ContentCategories(
            viewModelStoreOwner = remember { { ViewModelStore() } },
            block = BlocksState.Block.Categories(
                BlocksState.Block.Sections(),
                "Title",
                1L
            ),
            onCategoryClick = {}
        )
    }
}

@Composable
internal fun ContentCategories(
    viewModelStoreOwner: ViewModelStoreOwner,
    block: BlocksState.Block.Categories,
    onCategoryClick: (UsedeskCategory) -> Unit
) {
    val viewModel = kbUiViewModel(
        key = block.sectionId.toString(),
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> CategoriesViewModel(kbUiComponent.interactor, block.sectionId) }
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
            items = state.categories,
            key = UsedeskCategory::id
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardItem(
                        isTop = it == state.categories.firstOrNull(),
                        isBottom = it == state.categories.lastOrNull()
                    )
                    .clickableItem(
                        onClick = remember { { onCategoryClick(it) } }
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
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    style = TextStyle(
                        fontSize = 17.sp,
                        textAlign = TextAlign.Start,
                        color = colorResource(R.color.usedesk_black_2)
                    ),
                    text = it.title
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 10.dp)
                            .weight(weight = 1f, fill = true),
                        style = TextStyle(
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            color = colorResource(R.color.usedesk_gray_cold_2)
                        ),
                        text = it.description
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(24.dp),
                        painter = painterResource(R.drawable.usedesk_ic_arrow_forward),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        }
    }
}