package ru.usedesk.knowledgebase_gui.screen.blocks.search

import androidx.compose.foundation.lazy.LazyListState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.blocks.search.SearchViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class SearchViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.loadArticles().launchCollect { articlesModel ->
            setModel {
                val data = (articlesModel.loadingState as? LoadingState.Loaded)?.data
                copy(
                    reloadLoading = articlesModel.loadingState is LoadingState.Loading &&
                            articlesModel.loadingState.page == 1L,
                    reloadError = when (articlesModel.loadingState) {
                        is LoadingState.Error -> articlesModel.loadingState.page == 1L
                        is LoadingState.Loaded -> false
                        else -> reloadError
                    },
                    nextPageState = if (articlesModel.loadingState is LoadingState.Error &&
                        articlesModel.loadingState.page > 1L
                    ) State.NextPageState.ERROR
                    else if (articlesModel.hasNextPage) State.NextPageState.LOADING
                    else State.NextPageState.GONE,
                    content = when (data) {
                        null,
                        articles -> content
                        else -> articlesModel.loadingState.data.toItems()
                    },
                    itemShowedIndex = when (data) {
                        null,
                        articles -> itemShowedIndex
                        else -> when (articlesModel.loadingState.page) {
                            1L -> 0
                            else -> itemShowedIndex
                        }
                    },
                    articles = when (articlesModel.loadingState) {
                        is LoadingState.Loaded -> articlesModel.loadingState.data
                        else -> articles
                    },
                    lazyListState = when (data) {
                        null,
                        articles -> lazyListState
                        else -> when (articlesModel.page) {
                            1L -> LazyListState()
                            else -> lazyListState
                        }
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

    fun tryLoadAgain() {
        kbInteractor.loadArticles(reload = true)
    }

    fun tryNextPageAgain() {
        kbInteractor.loadArticles(nextPage = true)
    }

    fun lowestItemShowed() {
        setModel {
            val size = content?.size ?: 0
            UsedeskLog.onLog("lowestItemShowed") { size.toString() }
            when {
                size > itemShowedIndex -> copy(itemShowedIndex = size).apply {
                    kbInteractor.loadArticles(nextPage = true)
                }
                else -> this
            }
        }
    }

    data class State(
        val lazyListState: LazyListState = LazyListState(),
        val content: List<ArticleItem>? = null,
        val articles: List<UsedeskArticleContent> = listOf(),
        val reloadLoading: Boolean = true,
        val reloadError: Boolean = false,
        val nextPageState: NextPageState = NextPageState.LOADING,
        val itemShowedIndex: Int = 0
    ) {
        enum class NextPageState {
            LOADING,
            ERROR,
            GONE
        }
    }

    data class ArticleItem(
        val item: UsedeskArticleContent,
        val first: Boolean,
        val last: Boolean
    )
}