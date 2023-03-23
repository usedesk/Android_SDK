package ru.usedesk.knowledgebase_gui.screen.review

import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui.screen.review.ReviewViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.SendResult

internal class ReviewViewModel(
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

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
                buttonLoading = true,
                clearFocus = UsedeskEvent(Unit)
            ).updateButtonShowed()
        }
        val review = (state.selectedReplies + modelFlow.value.reviewValue.text)
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .joinToString(". ")
        knowledgeBase.sendReview(
            articleId,
            review,
            onResult = { result ->
                setModel {
                    when (result) {
                        SendResult.Done -> copy(
                            buttonLoading = false,
                            done = UsedeskEvent(Unit)
                        )
                        is SendResult.Error -> copy(
                            buttonLoading = false,
                            error = UsedeskEvent(result.code)
                        )
                    }.updateButtonShowed()

                }
            }
        )
    }

    private fun State.updateButtonShowed() = copy(
        buttonShowed = buttonLoading ||
                selectedReplies.isNotEmpty() ||
                reviewValue.text.any(Char::isLetterOrDigit)
    )

    data class State(
        val done: UsedeskEvent<Unit>? = null,
        val error: UsedeskEvent<Int?>? = null,
        val clearFocus: UsedeskEvent<Unit>? = null,
        val selectedReplies: List<String> = listOf(),
        val reviewValue: TextFieldValue = TextFieldValue(),
        val reviewFocused: Boolean = false,
        val buttonShowed: Boolean = false,
        val buttonLoading: Boolean = false
    )
}