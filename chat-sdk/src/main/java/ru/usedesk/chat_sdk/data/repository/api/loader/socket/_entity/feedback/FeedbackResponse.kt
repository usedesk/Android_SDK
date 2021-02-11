package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.feedback

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse

internal class FeedbackResponse : BaseResponse() {
    var answer: Answer? = null

    class Answer(
            var status: Boolean? = null
    )

    companion object {
        const val TYPE = "@@chat/current/CALLBACK_ANSWER"
    }
}