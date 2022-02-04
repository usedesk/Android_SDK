package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class SetClientRequest(
    token: String?,
    email: String?,
    name: String?,
    note: String?,
    phone: Long?,
    additionalId: String?
) : BaseRequest(TYPE) {

    private val payload: Payload = Payload(token, email, name, note, phone, additionalId)

    companion object {
        private const val TYPE = "@@server/chat/SET_CLIENT"
    }

    private class Payload(
        private val token: String?,
        private val email: String?,
        private val username: String?,
        private val note: String?,
        private val phone: Long?,
        @SerializedName("additional_id")
        private val additionalId: String?
    )
}