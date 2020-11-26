package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.SimpleMessage

class SimpleMessageResponse : BaseResponse(TYPE) {
    val message: SimpleMessage? = null

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"
    }
}