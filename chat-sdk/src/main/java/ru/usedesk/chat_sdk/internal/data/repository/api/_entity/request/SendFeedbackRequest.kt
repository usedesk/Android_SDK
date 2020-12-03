package ru.usedesk.chat_sdk.internal.data.repository.api._entity.request

import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback

class SendFeedbackRequest(
        token: String,
        feedback: UsedeskFeedback
) : BaseRequest(TYPE, token) {

    private val payload: Payload

    private class Payload internal constructor(feedback: UsedeskFeedback) {
        private val type: String = VALUE_FEEDBACK_ACTION

        private val data: UsedeskFeedback = feedback
    }

    companion object {
        private const val TYPE = "@@server/chat/CALLBACK"
        private const val VALUE_FEEDBACK_ACTION = "action"
    }

    init {
        payload = Payload(feedback)
    }
}