package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

class SendFeedbackResponse : BaseResponse(TYPE) {
    val answer: Answer? = null

    class Answer {
        private val isStatus = false
    }

    companion object {
        const val TYPE = "@@chat/current/CALLBACK_ANSWER"
    }
}