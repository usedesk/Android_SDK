
package ru.usedesk.chat_sdk.data.repository.form.entity

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SaveForm {
    class Request(
        @SerializedName("chat") val chat: String,
        @SerializedName("form") val form: List<Field>
    ) {
        class Field(
            @SerializedName("associate") val associate: String,
            @SerializedName("required") val required: Boolean,
            @SerializedName("value") val value: JsonElement,
            @SerializedName("label") val label: String,
        )
    }

    class Response(
        @SerializedName("status") val status: Int?,
        @SerializedName("fields") val fields: Map<String, JsonObject>?
    ) : UsedeskApiError()
}