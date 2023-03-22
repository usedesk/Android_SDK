package ru.usedesk.knowledgebase_gui.screen.review

import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui.screen.review.ReviewViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.SendReviewResult

internal class ReviewViewModel(
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

    fun reviewValueChanged(reviewValue: TextFieldValue) {
        setModel { copy(reviewValue = reviewValue).updateButtonShowed() }
    }

    fun problemSelected(problem: String) {
        setModel {
            copy(
                selectedProblems = when (problem) {
                    in selectedProblems -> selectedProblems - problem
                    else -> selectedProblems + problem
                }
            ).updateButtonShowed()
        }
    }

    fun sendClicked() {
        val state = setModel {
            copy(buttonLoading = true).updateButtonShowed()
        }
        val review = (state.selectedProblems + modelFlow.value.reviewValue.text)
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .joinToString(". ")
        knowledgeBase.sendReview(
            articleId,
            review,
            onResult = { result ->
                setModel {
                    copy(
                        buttonLoading = false,
                        done = when (result) {
                            SendReviewResult.Done -> UsedeskEvent(Unit)
                            is SendReviewResult.Error -> null
                        },
                        error = when (result) {
                            SendReviewResult.Done -> null
                            is SendReviewResult.Error -> UsedeskEvent(result.code)
                        }
                    ).updateButtonShowed()
                }
            }
        )
    }

    private fun State.updateButtonShowed() = copy(
        buttonShowed = buttonLoading ||
                selectedProblems.isNotEmpty() ||
                reviewValue.text.any(Char::isLetterOrDigit)
    )

    data class State(
        val done: UsedeskEvent<Unit>? = null,
        val error: UsedeskEvent<Int?>? = null,
        val selectedProblems: List<String> = listOf(),
        val reviewValue: TextFieldValue = TextFieldValue(),
        val buttonShowed: Boolean = false,
        val buttonLoading: Boolean = false
    )
}