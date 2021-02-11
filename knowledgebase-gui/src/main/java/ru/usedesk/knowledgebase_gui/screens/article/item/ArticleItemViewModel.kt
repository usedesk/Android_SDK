package ru.usedesk.knowledgebase_gui.screens.article.item

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleItemViewModel : UsedeskViewModel() {

    val articleContentLiveData = MutableLiveData(ArticleContentState.loading())

    fun init(articleId: Long) {
        doInit {
            doIt(UsedeskKnowledgeBaseSdk.requireInstance()
                    .getArticleRx(articleId), onValue = { articleContent ->
                articleContentLiveData.postValue(ArticleContentState.loaded(articleContent))
                doIt(UsedeskKnowledgeBaseSdk.requireInstance().addViewsRx(articleContent.id))
            }, onThrowable = {
                articleContentLiveData.postValue(ArticleContentState.failed())
            })
        }
    }

    fun sendArticleRating(articleId: Long, good: Boolean) {
        justDoIt(UsedeskKnowledgeBaseSdk.requireInstance()
                .sendRatingRx(articleId, good))
    }

    fun sendArticleRating(articleId: Long, message: String) {
        justDoIt(UsedeskKnowledgeBaseSdk.requireInstance()
                .sendRatingRx(articleId, message))
    }

    class ArticleContentState private constructor(
            val state: State,
            val articleContent: UsedeskArticleContent? = null
    ) {
        companion object {
            fun loading() = ArticleContentState(State.LOADING)
            fun failed() = ArticleContentState(State.FAILED)
            fun loaded(articleContent: UsedeskArticleContent) = ArticleContentState(State.LOADED, articleContent)
        }

        enum class State {
            LOADING,
            LOADED,
            FAILED
        }
    }
}