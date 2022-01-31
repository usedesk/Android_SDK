package ru.usedesk.knowledgebase_gui.screens.article.item

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleItemViewModel : UsedeskViewModel<ArticleItemViewModel.Model>(Model()) {

    fun init(articleId: Long) {
        doInit {
            doIt(
                UsedeskKnowledgeBaseSdk.requireInstance()
                    .getArticleRx(articleId), { articleContent ->
                    setModel { model ->
                        model.copy(
                            state = State.LOADED,
                            articleContent = articleContent
                        )
                    }
                    justDoIt(
                        UsedeskKnowledgeBaseSdk.requireInstance()
                            .addViewsRx(articleContent.id)
                    )
                }, {
                    setModel { model ->
                        model.copy(
                            state = State.FAILED,
                            articleContent = null
                        )
                    }
                })
        }
    }

    fun sendArticleRating(articleId: Long, good: Boolean) {
        justDoIt(
            UsedeskKnowledgeBaseSdk.requireInstance()
                .sendRatingRx(articleId, good)
        )
    }

    fun sendArticleRating(articleId: Long, message: String) {
        justDoIt(
            UsedeskKnowledgeBaseSdk.requireInstance()
                .sendRatingRx(articleId, message)
        )
    }

    data class Model(
        val state: State = State.LOADING,
        val articleContent: UsedeskArticleContent? = null
    )

    enum class State {
        LOADING,
        LOADED,
        FAILED
    }
}