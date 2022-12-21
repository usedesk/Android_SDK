package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.JsonObject
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadForm {
    class Request(
        val chat: String,
        val ids: String
    )

    class Response(
        val status: Int?,
        val fields: Map<String, JsonObject>?
    ) : UsedeskApiError()
}