package ru.usedesk.knowledgebase_gui.screen.compose.blocks.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.compose.cardItem
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.isSupportButtonVisible
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.compose.padding
import ru.usedesk.knowledgebase_gui.compose.rememberViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

@Preview
@Composable
private fun Preview() {
    val theme = UsedeskKnowledgeBaseTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
    ) {
        ContentCategories(
            theme = theme,
            viewModelStoreOwner = rememberViewModelStoreOwner { ViewModelStore() },
            sectionId = 1L,
            supportButtonVisible = remember { mutableStateOf(false) },
            onCategoryClick = {}
        )
    }
}

@Composable
internal fun ContentCategories(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreOwner: ViewModelStoreOwner,
    sectionId: Long,
    supportButtonVisible: MutableState<Boolean>,
    onCategoryClick: (UsedeskCategory) -> Unit
) {
    val viewModel = kbUiViewModel(
        key = sectionId.toString(),
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> CategoriesViewModel(kbUiComponent.interactor, sectionId) }
    val state by viewModel.modelFlow.collectAsState()
    supportButtonVisible.value = state.lazyListState.isSupportButtonVisible()
    LazyColumn(
        modifier = Modifier,
        state = state.lazyListState
    ) {
        items(
            items = state.categories,
            key = UsedeskCategory::id
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardItem(
                        theme = theme,
                        isTop = it == state.categories.firstOrNull(),
                        isBottom = it == state.categories.lastOrNull()
                    )
                    .clickableItem(
                        onClick = remember { { onCategoryClick(it) } }
                    )
                    .padding(theme.dimensions.categoriesItemInnerPadding)
            ) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = theme.textStyles.categoriesTitle,
                    text = it.title
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(weight = 1f, fill = true)
                            .padding(theme.dimensions.categoriesItemTitlePadding),
                        style = theme.textStyles.categoriesDescription,
                        text = it.description
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(theme.dimensions.categoriesItemArrowSize),
                        painter = painterResource(theme.drawables.iconListItemArrowForward),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        }
    }
}