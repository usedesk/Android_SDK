package ru.usedesk.knowledgebase_gui.screens.article.item

import io.reactivex.Completable
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
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
        setModel {
            copy(
                state = when (state) {
                    State.LOADING -> State.LOADING
                    else -> State.RELOADING
                },
                articleContent = null
            )
        }
        doIt(
            UsedeskKnowledgeBaseSdk.requireInstance()
                .getArticleRx(articleId), { articleContent ->
                setModel {
                    copy(
                        state = State.LOADED,
                        articleContent = articleContent
                    )
                }
                justDoIt(
                    UsedeskKnowledgeBaseSdk.requireInstance()
                        .addViewsRx(articleContent.id)
                )
            }, {
                setModel {
                    copy(
                        state = State.FAILED,
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
        val state: State = State.LOADING,
        val articleContent: UsedeskArticleContent? = null
    )
}