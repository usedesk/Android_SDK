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
        var lastLoadingState: LoadingState<List<UsedeskArticleContent>>? = null
        kbInteractor.articlesModelFlow.launchCollect { articlesModel ->
            setModel {
                when (articlesModel.loadingState) {
                    is LoadingState.Failed -> copy(
                        empty = false,
                        loading = false,
                        error = lastLoadingState != articlesModel.loadingState
                    )
                    is LoadingState.Loaded -> copy(
                        empty = articlesModel.loadingState.data.isEmpty(),
                        loading = false,
                        error = false,
                        articles = articlesModel.loadingState.data
                    )
                    is LoadingState.Loading -> copy(
                        loading = true
                    )
                }
            }
            lastLoadingState = articlesModel.loadingState
        }
    }

    fun tryAgain() {
        kbInteractor.loadArticles(
            query = kbInteractor.articlesModelFlow.value.query,
            reload = true
        )
    }

    data class State(
        val articles: List<UsedeskArticleContent> = listOf(),
        val empty: Boolean = false,
        val loading: Boolean = true,
        val error: Boolean = false
    )
}