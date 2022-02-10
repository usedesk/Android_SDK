package ru.usedesk.knowledgebase_gui.screens.article.item

import io.reactivex.Completable
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import java.util.concurrent.TimeUnit

internal class ArticleItemViewModel : UsedeskViewModel<ArticleItemViewModel.Model>(Model()) {

    fun init(articleId: Long) {
        doInit {
            reload(articleId)
        }
    }

    private fun reload(articleId: Long) {
        setModel { model ->
            model.copy(
                loading = true,
                articleContent = null
            )
        }
        doIt(
            UsedeskKnowledgeBaseSdk.requireInstance()
                .getArticleRx(articleId), { articleContent ->
                setModel { model ->
                    model.copy(
                        loading = true,
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
                        loading = false,
                        articleContent = null
                    )
                }
                doIt(Completable.timer(3, TimeUnit.SECONDS), {
                    reload(articleId)
                })
            })
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
        val loading: Boolean? = true,
        val articleContent: UsedeskArticleContent? = null
    )
}