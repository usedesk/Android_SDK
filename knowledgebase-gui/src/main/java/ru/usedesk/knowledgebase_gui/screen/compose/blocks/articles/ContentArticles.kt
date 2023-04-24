
package ru.usedesk.knowledgebase_gui.screen.compose.blocks.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

@Preview
@Composable
private fun Preview() {
    val theme = UsedeskKnowledgeBaseTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
    ) {
        ContentArticles(
            theme = theme,
            viewModelStoreOwner = rememberViewModelStoreOwner { ViewModelStore() },
            categoryId = 1L,
            supportButtonVisible = remember { mutableStateOf(false) },
            onArticleClick = {}
        )
    }
}

@Composable
internal fun ContentArticles(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreOwner: ViewModelStoreOwner,
    categoryId: Long,
    supportButtonVisible: MutableState<Boolean>,
    onArticleClick: (UsedeskArticleInfo) -> Unit
) {
    val viewModel = kbUiViewModel(
        key = categoryId.toString(),
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> ArticlesViewModel(kbUiComponent.interactor, categoryId) }
    val state by viewModel.modelFlow.collectAsState()
    supportButtonVisible.value = state.lazyListState.isSupportButtonVisible()
    LazyColumn(
        modifier = Modifier,
        state = state.lazyListState
    ) {
        items(
            items = state.articles,
            key = UsedeskArticleInfo::id
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardItem(
                        theme = theme,
                        isTop = remember(it, state.articles) { it == state.articles.firstOrNull() },
                        isBottom = remember(
                            it,
                            state.articles
                        ) { it == state.articles.lastOrNull() }
                    )
                    .clickableItem(onClick = remember { { onArticleClick(it) } })
                    .padding(theme.dimensions.articlesItemInnerPadding)
            ) {
                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(weight = 1f, fill = true)
                        .padding(theme.dimensions.articlesItemTitlePadding),
                    style = theme.textStyles.articlesItemTitle,
                    text = it.title
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(theme.dimensions.articlesItemArrowSize),
                    painter = painterResource(theme.drawables.iconListItemArrowForward),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
        }
    }
}