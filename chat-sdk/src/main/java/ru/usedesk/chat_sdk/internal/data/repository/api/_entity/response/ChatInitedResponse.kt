package ru.usedesk.chat_sdk.internal.data.repository.api._entity.response

import ru.usedesk.chat_sdk.internal.domain.entity.Message

internal class ChatInitedResponse : BaseResponse() {
    var token: String? = null
    var setup: Setup? = null
    var noOperators: Boolean? = null

    companion object {
        const val TYPE = "@@chat/current/INITED"
    }

    class Setup {
        var waitingEmail: Boolean? = null
        var isNoOperators: Boolean? = null
        var messages: List<Message?>? = null
    }
}