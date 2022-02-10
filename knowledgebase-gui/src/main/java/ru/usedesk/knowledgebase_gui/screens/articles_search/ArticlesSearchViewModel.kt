package ru.usedesk.knowledgebase_gui.screens.articles_search

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
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
        if (lastQuery != searchQuery || modelLiveData.value.loading != null) {
            lastQuery = searchQuery
            loadingDisposable?.dispose()
            loadingDisposable = doIt(
                UsedeskKnowledgeBaseSdk.requireInstance()
                    .getArticlesRx(searchQuery), {
                    setModel { model ->
                        model.copy(
                            articles = it,
                            loading = null
                        )
                    }
                }, {
                    setModel { model ->
                        model.copy(
                            articles = listOf(),
                            loading = false
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
        val loading: Boolean? = true
    )
}