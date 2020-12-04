package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class SetEmailRequest(
        token: String,
        private val email: String,
        name: String?,
        phone: Long?,
        additionalId: Long?
) : BaseRequest(TYPE, token) {

    private val payload: Payload

    init {
        payload = Payload(email, name, phone, additionalId)
    }

    companion object {
        private const val TYPE = "@@server/chat/SET_EMAIL"
    }

    class Payload(
            private val email: String,
            private val name: String?,
            private val phone: Long?,
            private val additionalId: Long?
    )
}