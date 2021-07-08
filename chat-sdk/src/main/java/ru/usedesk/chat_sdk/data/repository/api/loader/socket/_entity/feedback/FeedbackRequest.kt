package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.feedback

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest
import ru.usedesk.chat_sdk.entity.UsedeskFeedback

internal class FeedbackRequest(
    messageId: Long,
    feedback: UsedeskFeedback
) : BaseRequest(TYPE) {

    private val payload = Payload(feedback, messageId)

    private class Payload(
        private val data: UsedeskFeedback,
        private val messageId: Long
    ) {
        private val type: String = VALUE_FEEDBACK_ACTION
    }

    companion object {
        private const val TYPE = "@@server/chat/CALLBACK"
        private const val VALUE_FEEDBACK_ACTION = "action"
    }
}