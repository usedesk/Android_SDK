package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile

class SendMessageRequest(
        token: String,
        text: String? = " ",
        usedeskFile: UsedeskFile? = null
) : BaseRequest(TYPE, token) {

    private val message = RequestMessage(text, usedeskFile)

    companion object {
        private const val TYPE = "@@server/chat/SEND_MESSAGE"
    }

    private class RequestMessage(
            private val text: String?,
            private val usedeskFile: UsedeskFile?
    )
}