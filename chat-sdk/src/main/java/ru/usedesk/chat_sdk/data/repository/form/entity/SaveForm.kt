
package ru.usedesk.chat_sdk.data.repository.form.entity

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SaveForm {
    class Request(
        val chat: String,
        val form: List<Field>
    ) {
        class Field(
            val associate: String,
            val required: Boolean,
            val value: JsonElement
        )
    }

    class Response(
        val status: Int?,
        val fields: Map<String, JsonObject>?
    ) : UsedeskApiError()
}