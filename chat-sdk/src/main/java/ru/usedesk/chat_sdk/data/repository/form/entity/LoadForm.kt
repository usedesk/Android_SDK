
package ru.usedesk.chat_sdk.data.repository.form.entity

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadForm {
    class Request(
        @SerializedName("chat") val chat: String,
        @SerializedName("ids") val ids: String
    )

    class Response(
        @SerializedName("status") val status: Int?,
        @SerializedName("fields") val fields: Map<String, JsonObject>?
    ) : UsedeskApiError() {

        class FieldLoadedList(
            @SerializedName("id") val id: String,
            @SerializedName("name") val name: String,
            @SerializedName("children") val children: Array<Children>
        ) {
            class Children(
                @SerializedName("id") val id: Long,
                @SerializedName("value") val value: String,
                @SerializedName("parent_option_id") val parentOptionId: Array<Long>?
            )
        }
    }
}