package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.PayloadMessage

class PayloadMessageResponse : BaseResponse(TYPE) {
    val message: PayloadMessage? = null

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"
    }
}