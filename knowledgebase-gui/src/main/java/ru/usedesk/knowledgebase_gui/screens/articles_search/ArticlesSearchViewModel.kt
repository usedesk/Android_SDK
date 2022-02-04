package ru.usedesk.knowledgebase_gui.screens.articles_search

import io.reactivex.disposables.Disposable
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticlesSearchViewModel : UsedeskViewModel<ArticlesSearchViewModel.Model>(Model()) {

    private var lastQuery: String? = null
    private var loadingDisposable: Disposable? = null

    init {
        onSearchQuery("")
    }

    fun onSearchQuery(searchQuery: String) {
        if (lastQuery != searchQuery || modelLiveData.value.state != State.LOADED) {
            lastQuery = searchQuery
            loadingDisposable?.dispose()
            loadingDisposable = doIt(UsedeskKnowledgeBaseSdk.requireInstance()
                .getArticlesRx(searchQuery), {
                setModel { model ->
                    model.copy(
                        articles = it,
                        state = if (it.isEmpty()) {
                            State.EMPTY
                        } else {
                            State.LOADED
                        }
                    )
                }
            }, {
                setModel { model ->
                    model.copy(
                        articles = listOf(),
                        state = State.EMPTY
                    )
                }
            })
        }
    }

    data class Model(
        val articles: List<UsedeskArticleContent> = listOf(),
        val state: State = State.LOADING
    )

    enum class State {
        LOADING,
        EMPTY,
        LOADED
    }
}