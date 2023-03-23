package ru.usedesk.knowledgebase_gui.screen.article

import androidx.compose.foundation.ScrollState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State.RatingState
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.GetArticleResult
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.SendResult
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleViewModel(
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()
    val scrollState = ScrollState(0)

    init {
        loadArticle()
    }

    private fun loadArticle() {
        knowledgeBase.getArticle(articleId) { result ->
            setModel {
                copy(
                    content = when (result) {
                        is GetArticleResult.Done -> State.Content.Article(result.articleContent)
                        is GetArticleResult.Error -> when (content) {
                            is State.Content.Article -> content
                            is State.Content.Loading -> content.copy(
                                loading = false,
                                error = true
                            )
                        }
                    }
                )
            }
        }
    }

    fun onRating(good: Boolean) {
        setModel { copy(ratingState = if (good) RatingState.GOOD_SENDING else RatingState.BAD_SENDING) }
        knowledgeBase.sendRating(
            articleId,
            good
        ) { result ->
            setModel {
                when (result) {
                    SendResult.Done -> copy(
                        ratingState = RatingState.RATED,
                        goReview = if (!good) UsedeskEvent(Unit) else null
                    )
                    is SendResult.Error -> copy(
                        ratingState = RatingState.NEED,
                        error = UsedeskEvent(result.code)
                    )
                }
            }
        }
    }

    fun tryAgain() {
        loadArticle()
    }

    data class State(
        val content: Content = Content.Loading(),
        val ratingState: RatingState = RatingState.NEED,
        val goReview: UsedeskEvent<Unit>? = null,
        val error: UsedeskEvent<Int?>? = null
    ) {
        enum class RatingState {
            NEED,
            GOOD_SENDING,
            BAD_SENDING,
            RATED
        }

        sealed interface Content {
            data class Loading(
                val loading: Boolean = true,
                val error: Boolean = false
            ) : Content

            data class Article(val articleContent: UsedeskArticleContent) : Content
        }
    }
}