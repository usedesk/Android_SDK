package ru.usedesk.chat_sdk.internal.data.repository.api._entity.response

internal class FeedbackResponse : BaseResponse() {
    var answer: Answer? = null

    class Answer(
            var isStatus: Boolean? = null
    )

    companion object {
        const val TYPE = "@@chat/current/CALLBACK_ANSWER"
    }
}