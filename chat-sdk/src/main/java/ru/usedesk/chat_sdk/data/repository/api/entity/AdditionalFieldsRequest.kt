package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class AdditionalFieldsRequest(
    @SerializedName("chat_token")
    private val chatToken: String,
    @SerializedName("additional_fields")
    private val additionalFields: List<AdditionalField>
) {

    class AdditionalField(
        private val id: Long,
        private val value: String
    )
}