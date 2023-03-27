package ru.usedesk.knowledgebase_gui.screen.article

import androidx.compose.foundation.ScrollState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor,
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {
    val scrollState = ScrollState(0)

    init {
        kbInteractor.articleModelFlow.launchCollect { articleModel ->
            setModel {
                copy(
                    loadingState = when (loadingState) {
                        is LoadingState.Loading,
                        is LoadingState.Failed -> articleModel.loadingState
                        is LoadingState.Loaded -> loadingState
                    },
                    ratingState = articleModel.ratingState,
                    reviewExpected = when (articleModel.ratingState) {
                        RatingState.Required,
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
        val loadingState: LoadingState<UsedeskArticleContent> = LoadingState.Loading(),
        val ratingState: RatingState = RatingState.Required,
        val reviewExpected: Boolean = false,
        val goReview: UsedeskEvent<Unit>? = null,
        val error: UsedeskEvent<Int?>? = null
    )
}