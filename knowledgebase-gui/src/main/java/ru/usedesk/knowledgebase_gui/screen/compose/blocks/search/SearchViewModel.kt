
package ru.usedesk.knowledgebase_gui.screen.compose.blocks.search

import androidx.compose.foundation.lazy.LazyListState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.ArticlesModel.SearchItem
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.search.SearchViewModel.State

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
                    nextPageState = when {
                        articlesModel.loadingState is LoadingState.Error &&
                                articlesModel.loadingState.page > 1L -> State.NextPageState.ERROR
                        articlesModel.hasNextPage -> State.NextPageState.LOADING
                        else -> State.NextPageState.GONE
                    },
                    content = when (data) {
                        null -> content
                        else -> articlesModel.loadingState.data
                    },
                    itemShowedIndex = when {
                        data == null || data === content -> itemShowedIndex
                        else -> when (articlesModel.loadingState.page) {
                            1L -> 0
                            else -> itemShowedIndex
                        }
                    },
                    lazyListState = when (data) {
                        null,
                        content -> lazyListState
                        else -> when (articlesModel.page) {
                            1L -> LazyListState()
                            else -> lazyListState
                        }
                    }
                )
            }
        }
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
        val content: List<SearchItem>? = null,
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
}