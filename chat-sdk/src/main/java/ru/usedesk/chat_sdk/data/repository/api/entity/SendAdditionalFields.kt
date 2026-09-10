
package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SendAdditionalFields {
    class Request(
        @SerializedName("chat_token") private val chatToken: String?,
        @SerializedName("additional_fields") private val additionalFields: List<AdditionalField>
    ) {

        class AdditionalField(
            @SerializedName("id") private val id: Long,
            @SerializedName("value") private val value: String
        )
    }

    class Response(
        @SerializedName("status") val status: Int? = null
    ) : UsedeskApiError()
}