package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class SetClientRequest(
    token: String?,
    email: String?,
    name: String?,
    note: String?,
    phone: Long?,
    additionalId: Long?,
    avatar: Avatar?
) : BaseRequest(TYPE) {

    private val payload: Payload = Payload(token, email, name, note, phone, additionalId, avatar)

    companion object {
        private const val TYPE = "@@server/chat/SET_CLIENT"
    }

    private class Payload(
        private val token: String?,
        private val email: String?,
        private val username: String?,
        private val note: String?,
        private val phone: Long?,
        private val additionalId: Long?,
        private val avatar: Avatar?
    )

    internal class Avatar(
        private val name: String,
        private val content: String
    )
}