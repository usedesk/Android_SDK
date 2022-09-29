package ru.usedesk.knowledgebase_gui.screens.articles_search

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import java.util.concurrent.TimeUnit

internal class ArticlesSearchViewModel : UsedeskViewModel<ArticlesSearchViewModel.Model>(Model()) {

    private var lastQuery: String? = null
    private var loadingDisposable: Disposable? = null

    init {
        onSearchQuery("")
    }

    fun onSearchQuery(searchQuery: String) {
        if (lastQuery != searchQuery || modelFlow.value.state == State.FAILED) {
            lastQuery = searchQuery
            loadingDisposable?.dispose()
            setModel {
                copy(
                    articles = listOf(),
                    state = when (state) {
                        in listOf(State.LOADING, State.LOADED) -> State.LOADING
                        else -> State.RELOADING
                    }
                )
            }
            loadingDisposable = doIt(
                UsedeskKnowledgeBaseSdk.requireInstance()
                    .getArticlesRx(searchQuery), {
                    setModel {
                        copy(
                            articles = it,
                            state = State.LOADED
                        )
                    }
                }, {
                    setModel {
                        copy(
                            articles = listOf(),
                            state = State.FAILED
                        )
                    }
                    doIt(Completable.timer(3, TimeUnit.SECONDS), {
                        onSearchQuery(searchQuery)
                    })
                })
        }
    }

    data class Model(
        val articles: List<UsedeskArticleContent> = listOf(),
        val state: State = State.LOADING
    )
}