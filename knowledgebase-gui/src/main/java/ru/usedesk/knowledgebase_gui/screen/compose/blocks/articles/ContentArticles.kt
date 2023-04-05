package ru.usedesk.knowledgebase_gui.screen.compose.blocks.articles

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.compose.cardItem
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.isSupportButtonVisible
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
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
            viewModelStoreOwner = remember { { ViewModelStore() } },
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
                    .padding(
                        start = 20.dp,
                        end = 10.dp,
                        top = 24.dp,
                        bottom = 24.dp
                    )
            ) {
                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(weight = 1f, fill = true),
                    style = theme.textStyles.articlesItemTitle,
                    text = it.title
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 10.dp)
                        .size(24.dp),
                    painter = painterResource(theme.drawables.iconListItemArrowForward),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
        }
    }
}