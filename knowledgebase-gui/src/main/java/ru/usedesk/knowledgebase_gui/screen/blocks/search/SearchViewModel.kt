package ru.usedesk.knowledgebase_gui.screen.blocks.search

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
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
                        loading = false,
                        error = when (lastLoadingState) {
                            articlesModel.loadingState -> null
                            else -> UsedeskEvent(Unit)
                        }
                    )
                    is LoadingState.Loaded -> copy(
                        loading = false,
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

    fun onQuery(query: String) {
        kbInteractor.loadArticles(query)
    }

    data class State(
        val articles: List<UsedeskArticleContent> = listOf(),
        val loading: Boolean = true,
        val error: UsedeskEvent<Unit>? = null
    )
}