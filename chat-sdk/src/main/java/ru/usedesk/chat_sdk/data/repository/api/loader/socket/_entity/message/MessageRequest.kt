package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class MessageRequest(
        token: String,
        text: String
) : BaseRequest(TYPE, token) {

    private val message = RequestMessage(text)

    companion object {
        private const val TYPE = "@@server/chat/SEND_MESSAGE"
    }

    private class RequestMessage(
            private val text: String
    )
}