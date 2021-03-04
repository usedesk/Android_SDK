package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse

internal class InitChatResponse : BaseResponse() {
    var token: String? = null
    var setup: Setup? = null

    companion object {
        const val TYPE = "@@chat/current/INITED"
    }

    class Setup {
        var noOperators: Boolean? = null
        var messages: List<MessageResponse.Message?>? = null
    }
}