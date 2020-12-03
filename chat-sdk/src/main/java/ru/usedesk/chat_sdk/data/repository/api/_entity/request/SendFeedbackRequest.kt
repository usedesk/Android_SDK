package ru.usedesk.chat_sdk.data.repository.api._entity.request

import ru.usedesk.chat_sdk.entity.UsedeskFeedback

class SendFeedbackRequest(
        token: String,
        feedback: UsedeskFeedback
) : BaseRequest(TYPE, token) {

    private val payload = Payload(feedback)

    private class Payload(private val data: UsedeskFeedback) {
        private val type: String = VALUE_FEEDBACK_ACTION
    }

    companion object {
        private const val TYPE = "@@server/chat/CALLBACK"
        private const val VALUE_FEEDBACK_ACTION = "action"
    }
}