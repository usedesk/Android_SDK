package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse

internal class SetClientResponse : BaseResponse() {
    var state: State? = null
    var reset: Boolean? = null

    class State {
        var client: Client? = null

        class Client {
            var token: String? = null
            var email: String? = null
            var chat: Int? = null
        }
    }

    companion object {
        const val TYPE = "@@chat/current/SET"
    }
}