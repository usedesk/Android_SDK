package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request

class SendMessageRequest(
        token: String,
        val requestMessage: RequestMessage
) : BaseRequest(TYPE, token) {

    companion object {
        private const val TYPE = "@@server/chat/SEND_MESSAGE"
    }
}