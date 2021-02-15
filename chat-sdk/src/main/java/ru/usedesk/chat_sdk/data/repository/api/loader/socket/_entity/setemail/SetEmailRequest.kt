package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class SetEmailRequest(
        token: String,
        email: String?,
        name: String?,
        note: String?,
        phone: Long?,
        additionalId: Long?
) : BaseRequest(TYPE, token) {

    private val payload: Payload = Payload(email, name, note, phone, additionalId)

    companion object {
        private const val TYPE = "@@server/chat/SET_EMAIL"
    }

    class Payload(
            private val email: String?,
            private val username: String?,
            private val note: String?,
            private val phone: Long?,
            private val additionalId: Long?
    )
}