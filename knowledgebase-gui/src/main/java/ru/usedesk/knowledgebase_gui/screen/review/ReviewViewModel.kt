package ru.usedesk.knowledgebase_gui.screen.review

import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui._entity.ReviewState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.review.ReviewViewModel.State

internal class ReviewViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor,
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {

    init {
        var lastReviewState: ReviewState? = null
        kbInteractor.articleModelFlow.launchCollect { articleModel ->
            setModel {
                when (articleModel.reviewState) {
                    ReviewState.Required -> copy(
                        buttonLoading = false,
                        error = null,
                        goBackExpected = false
                    )
                    ReviewState.Sending -> copy(
                        buttonLoading = true
                    )
                    is ReviewState.Failed -> copy(
                        buttonLoading = false,
                        goBackExpected = false,
                        error = when (lastReviewState) {
                            articleModel.reviewState -> null
                            else -> UsedeskEvent(articleModel.reviewState.code)
                        }
                    )
                    ReviewState.Sent -> copy(
                        buttonLoading = false,
                        goBackExpected = false,
                        goBack = if (goBackExpected) UsedeskEvent(Unit) else null
                    )
                }.updateButtonShowed()
            }
            lastReviewState = articleModel.reviewState
        }
    }

    fun reviewValueChanged(reviewValue: TextFieldValue) {
        setModel { copy(reviewValue = reviewValue).updateButtonShowed() }
    }

    fun reviewFocusChanged(focused: Boolean) {
        setModel { copy(reviewFocused = focused) }
    }

    fun replySelected(problem: String) {
        setModel {
            copy(
                selectedReplies = when (problem) {
                    in selectedReplies -> selectedReplies - problem
                    else -> selectedReplies + problem
                }
            ).updateButtonShowed()
        }
    }

    fun sendClicked() {
        val state = setModel {
            copy(
                clearFocus = UsedeskEvent(Unit),
                goBackExpected = true
            ).updateButtonShowed()
        }
        val review = (state.selectedReplies + modelFlow.value.reviewValue.text)
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .joinToString(". ")
        kbInteractor.sendReview(
            articleId,
            review
        )
    }

    private fun State.updateButtonShowed() = copy(
        buttonShowed = buttonLoading ||
                selectedReplies.isNotEmpty() ||
                reviewValue.text.any(Char::isLetterOrDigit)
    )

    data class State(
        val goBack: UsedeskEvent<Unit>? = null,
        val error: UsedeskEvent<Int?>? = null,
        val goBackExpected: Boolean = false,
        val clearFocus: UsedeskEvent<Unit>? = null,
        val selectedReplies: List<String> = listOf(),
        val reviewValue: TextFieldValue = TextFieldValue(),
        val reviewFocused: Boolean = false,
        val buttonShowed: Boolean = false,
        val buttonLoading: Boolean = false
    )
}