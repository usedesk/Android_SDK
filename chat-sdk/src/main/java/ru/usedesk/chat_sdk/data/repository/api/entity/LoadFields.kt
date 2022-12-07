package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.JsonObject
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadFields {
    class Request(
        val chat: String,
        val ids: List<Long>
    )

    class Response(
        val status: Int?,
        val fields: Set<Map.Entry<String, JsonObject>>?
    ) : UsedeskApiError()
}