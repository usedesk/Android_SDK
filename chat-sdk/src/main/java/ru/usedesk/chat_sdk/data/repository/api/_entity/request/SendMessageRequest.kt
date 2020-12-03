package ru.usedesk.chat_sdk.data.repository.api._entity.request

import ru.usedesk.chat_sdk.data._entity.UsedeskFile

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