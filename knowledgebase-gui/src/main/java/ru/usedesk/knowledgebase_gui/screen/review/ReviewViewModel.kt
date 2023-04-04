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
        kbInteractor.loadArticle(articleId).launchCollect { articleModel ->
            setModel {
                when (articleModel.reviewState) {
                    is ReviewState.Required -> copy(
                        buttonLoading = false,
                        buttonError = articleModel.reviewState.error,
                        goBackExpected = false
                    )
                    ReviewState.Sending -> copy(
                        buttonLoading = true,
                        buttonError = false
                    )
                    ReviewState.Sent -> copy(
                        buttonLoading = false,
                        goBackExpected = false,
                        goBack = if (goBackExpected) UsedeskEvent(Unit) else null
                    )
                }.updateButtonShowed()
            }
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

    fun sendClicked(
        tagsPrefix: String,
        commentPrefix: String
    ) {
        val state = setModel {
            copy(
                clearFocus = UsedeskEvent(Unit),
                goBackExpected = true
            ).updateButtonShowed()
        }

        val subject = "ID: $articleId"
        val message = listOfNotNull(
            when (state.selectedReplies.size) {
                0 -> null
                else -> "$tagsPrefix: ${state.selectedReplies.joinToString(". ")}."
            }, when (val comment = state.reviewValue.text) {
                "" -> null
                else -> "$commentPrefix: $comment"
            }
        ).joinToString("\n")
        kbInteractor.sendReview(
            articleId,
            subject,
            message
        )
    }

    private fun State.updateButtonShowed() = copy(
        buttonShowed = buttonLoading ||
                selectedReplies.isNotEmpty() ||
                reviewValue.text.any(Char::isLetterOrDigit)
    )

    data class State(
        val goBack: UsedeskEvent<Unit>? = null,
        val goBackExpected: Boolean = false,
        val clearFocus: UsedeskEvent<Unit>? = null,
        val selectedReplies: List<String> = listOf(),
        val reviewValue: TextFieldValue = TextFieldValue(),
        val reviewFocused: Boolean = false,
        val buttonShowed: Boolean = false,
        val buttonLoading: Boolean = false,
        val buttonError: Boolean = false
    )
}