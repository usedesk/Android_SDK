package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message

import ru.usedesk.chat_sdk.data._entity.Message
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse

internal class MessageResponse : BaseResponse() {

    var message: Message? = null

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"
    }
}