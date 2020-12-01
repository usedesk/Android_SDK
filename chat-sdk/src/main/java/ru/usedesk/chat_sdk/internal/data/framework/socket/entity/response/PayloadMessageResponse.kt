package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

import ru.usedesk.chat_sdk.external.entity.UsedeskPayload
import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage

class PayloadMessageResponse : BaseResponse(TYPE) {
    val message: PayloadMessage? = null

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"
    }

    class PayloadMessage : BaseMessage() {
        val payload: UsedeskPayload? = null
    }
}