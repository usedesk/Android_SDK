package ru.usedesk.chat_sdk.internal.domain.entity

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.InitChatResponse

interface OnMessageListener {
    fun onInit(initChatResponse: InitChatResponse)

    fun onInitChat()

    fun onNew(message: UsedeskMessage)

    fun onFeedback()

    fun onTokenError()
}