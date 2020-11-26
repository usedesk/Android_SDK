package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage

class NewMessageResponse(val message: UsedeskMessage) : BaseResponse(TYPE) {

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"
    }
}