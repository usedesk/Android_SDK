package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class MessageRequest(
    text: String,
    messageId: Long
) : BaseRequest(TYPE) {

    private val message = RequestMessage(text, messageId)

    companion object {
        private const val TYPE = "@@server/chat/SEND_MESSAGE"
    }

    private class RequestMessage(
        private val text: String,
        messageId: Long
    ) {
        private val payload = Payload(messageId)
    }

    private class Payload(
        private val messageId: Long
    )
}