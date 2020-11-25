package ru.usedesk.chat_sdk.internal.domain.entity

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup

interface OnMessageListener {
    fun onInit(token: String, setup: Setup)

    fun onInitChat()

    fun onNew(message: UsedeskMessage)

    fun onFeedback()

    fun onTokenError()
}