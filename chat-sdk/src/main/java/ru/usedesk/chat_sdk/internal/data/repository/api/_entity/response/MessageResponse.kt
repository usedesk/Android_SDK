package ru.usedesk.chat_sdk.internal.data.repository.api._entity.response

import ru.usedesk.chat_sdk.internal.domain.entity.Message

internal class MessageResponse : BaseResponse() {

    var message: Message? = null

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"
    }
}