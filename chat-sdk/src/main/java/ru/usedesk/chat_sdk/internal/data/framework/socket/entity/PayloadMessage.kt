package ru.usedesk.chat_sdk.internal.data.framework.socket.entity

import ru.usedesk.chat_sdk.external.entity.UsedeskPayload
import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage

class PayloadMessage : BaseMessage() {
    val payload: UsedeskPayload? = null
}