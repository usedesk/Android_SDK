package ru.usedesk.chat_sdk.data.repository.api.entity

internal class AdditionalFieldsRequest(
    private val chatToken: String,
    private val additionalFields: List<AdditionalField>
) {

    class AdditionalField(
        private val id: Long,
        private val value: String
    )
}