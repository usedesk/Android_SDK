package ru.usedesk.knowledgebase_gui.screen.blocks.search

import ru.usedesk.common_gui.UsedeskViewModel
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
                when (articlesModel.loadingState) {
                    is LoadingState.Loading -> copy(
                        loading = articlesModel.loadingState.loading,
                        error = articlesModel.loadingState.error,
                        empty = if (articlesModel.loadingState.error) false else empty
                    )
                    is LoadingState.Loaded -> copy(
                        empty = articlesModel.loadingState.data.isEmpty(),
                        loading = false,
                        error = false,
                        articles = articlesModel.loadingState.data
                    )
                }
            }
        }
    }

    fun tryAgain() {
        kbInteractor.loadArticles(reload = true)
    }

    fun itemShowed(itemIndex: Int) {
    }

    data class State(
        val articles: List<UsedeskArticleContent> = listOf(),
        val empty: Boolean = false,
        val loading: Boolean = true,
        val error: Boolean = false
    )
}