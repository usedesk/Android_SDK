
package ru.usedesk.knowledgebase_gui.screen.compose.article

import androidx.compose.foundation.ScrollState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.compose.article.ArticleViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor,
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.loadArticle(articleId).launchCollect { articleModel ->
            setModel {
                copy(
                    contentState = contentState.update(
                        loadingState = articleModel.loadingState,
                        convert = { this }
                    ),
                    loading = when (articleModel.loadingState) {
                        is LoadingState.Loading -> true
                        is LoadingState.Loaded -> !articleShowed
                        else -> false
                    },
                    ratingState = articleModel.ratingState,
                    reviewExpected = when (articleModel.ratingState) {
                        is RatingState.Required,
                        is RatingState.Sent -> false
                        is RatingState.Sending -> reviewExpected
                    },
                    goReview = when {
                        reviewExpected &&
                                articleModel.ratingState is RatingState.Sent &&
                                !articleModel.ratingState.good -> UsedeskEvent(Unit)
                        else -> null
                    }
                )
            }
        }
        kbInteractor.loadArticle(articleId)
    }

    fun articleHidden() {
        setModel {
            copy(
                articleShowed = false,
                loading = true
            )
        }
    }

    fun articleShowed() {
        setModel {
            copy(
                articleShowed = true,
                loading = contentState !is ContentState.Loaded
            )
        }
    }

    fun onRating(good: Boolean) {
        setModel { copy(reviewExpected = true) }
        kbInteractor.sendRating(
            articleId,
            good
        )
    }

    fun tryAgain() {
        kbInteractor.loadArticle(articleId)
    }

    data class State(
        val scrollState: ScrollState = ScrollState(0),
        val contentState: ContentState<UsedeskArticleContent> = ContentState.Empty(),
        val loading: Boolean = true,
        val articleShowed: Boolean = false,
        val ratingState: RatingState = RatingState.Required(),
        val reviewExpected: Boolean = false,
        val goReview: UsedeskEvent<Unit>? = null
    )
}