
package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SendAdditionalFields {
    class Request(
        private val chatToken: String,
        private val additionalFields: List<AdditionalField>
    ) {

        class AdditionalField(
            private val id: Long,
            private val value: String
        )
    }

    class Response(
        val status: Int? = null
    ) : UsedeskApiError()
}