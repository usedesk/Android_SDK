package ru.usedesk.chat_sdk.data.repository.api._entity.response

import ru.usedesk.chat_sdk.data._entity.Message

internal class ChatInitedResponse : BaseResponse() {
    var token: String? = null
    var setup: Setup? = null

    companion object {
        const val TYPE = "@@chat/current/INITED"
    }

    class Setup {
        var waitingEmail: Boolean? = null
        var noOperators: Boolean? = null
        var messages: List<Message?>? = null
    }
}