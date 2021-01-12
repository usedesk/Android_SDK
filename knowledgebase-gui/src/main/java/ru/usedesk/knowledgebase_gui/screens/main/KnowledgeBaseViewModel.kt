package ru.usedesk.knowledgebase_gui.screens.main

import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.release
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class KnowledgeBaseViewModel : UsedeskViewModel() {

    val searchQueryLiveData = MutableLiveData<String>()
    private val delayedQuery: DelayedQuery = DelayedQuery(searchQueryLiveData, SEARCH_DELAY)
    val articleContentLiveData = MutableLiveData(ArticleContentState.none())

    private var articleContentDisposable: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        delayedQuery.dispose()
        release()
    }

    fun onSearchQuery(query: String) {
        delayedQuery.onNext(query)
    }

    fun onArticleClick(articleId: Long) {
        articleContentLiveData.postValue(ArticleContentState.loading())
        articleContentDisposable = UsedeskKnowledgeBaseSdk.getInstance()
                .getArticleRx(articleId)
                .subscribe({ articleContent ->
                    articleContentLiveData.postValue(ArticleContentState.success(articleContent))
                    doIt(UsedeskKnowledgeBaseSdk.getInstance().addViewsRx(articleContent.id))
                }, {
                    articleContentLiveData.postValue(ArticleContentState.error())
                })
    }

    fun onArticleClosed() {
        articleContentDisposable?.apply {
            dispose()
            articleContentDisposable = null
        }
        articleContentLiveData.postValue(ArticleContentState.none())
    }

    fun sendArticleFeedback(articleId: Long, good: Boolean) {
        justDoIt(UsedeskKnowledgeBaseSdk.getInstance()
                .sendFeedbackRx(articleId, good))
    }

    fun sendArticleFeedback(articleId: Long, message: String) {
        justDoIt(UsedeskKnowledgeBaseSdk.getInstance()
                .sendFeedbackRx(articleId, message))
    }

    companion object {
        private const val SEARCH_DELAY = 500
    }

    class ArticleContentState private constructor(
            val state: State,
            val articleContent: UsedeskArticleContent? = null
    ) {
        companion object {
            fun none() = ArticleContentState(State.NONE)
            fun loading() = ArticleContentState(State.LOADING)
            fun error() = ArticleContentState(State.ERROR)
            fun success(articleContent: UsedeskArticleContent) = ArticleContentState(State.SUCCESS, articleContent)
        }

        enum class State {
            NONE,
            LOADING,
            ERROR,
            SUCCESS
        }
    }
}