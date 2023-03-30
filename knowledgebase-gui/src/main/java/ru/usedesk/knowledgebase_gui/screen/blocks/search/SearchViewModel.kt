package ru.usedesk.knowledgebase_gui.screen.blocks.search

import androidx.compose.foundation.lazy.LazyListState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.blocks.search.SearchViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class SearchViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor
) : UsedeskViewModel<State>(State()) {

    var lazyListState: LazyListState = LazyListState()

    init {
        kbInteractor.loadArticles().launchCollect { articlesModel ->
            setModel {
                if (articles != articlesModel.loadingState.data && articlesModel.page == 1L) {
                    lazyListState = LazyListState()
                }
                copy(
                    contentState = contentState.update(
                        loadingState = articlesModel.loadingState,
                        convert = { }
                    ),
                    content = when (articles) {
                        articlesModel.loadingState.data -> null
                        else -> articlesModel.loadingState.data?.toItems()
                    } ?: content,
                    loading = articlesModel.loadingState is LoadingState.Loading,
                    query = articlesModel.query,
                    articles = when (articlesModel.loadingState) {
                        is LoadingState.Loaded -> articlesModel.loadingState.data
                        else -> articles
                    }
                )
            }
        }
    }

    private fun List<UsedeskArticleContent>.toItems() = mapIndexed { index, item ->
        ArticleItem(
            item,
            first = index == 0,
            last = index == size - 1
        )
    }

    fun tryAgain() {
        kbInteractor.loadArticles(reload = true)
    }

    fun lowestItemShowed() {
        kbInteractor.loadArticles(nextPage = true)
    }

    data class State(
        val contentState: ContentState<Unit> = ContentState.Empty(),
        val content: List<ArticleItem> = listOf(),
        val articles: List<UsedeskArticleContent> = listOf(),
        val loading: Boolean = true,
        val query: String = ""
    )

    data class ArticleItem(
        val item: UsedeskArticleContent,
        val first: Boolean,
        val last: Boolean
    )
}