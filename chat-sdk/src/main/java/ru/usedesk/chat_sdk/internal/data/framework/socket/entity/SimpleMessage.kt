package ru.usedesk.chat_sdk.internal.data.framework.socket.entity

import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage

class SimpleMessage : BaseMessage() {
    val payload: String? = null
}