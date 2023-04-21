
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface CreateTicket {
    class Request(
        private val apiToken: String,
        private val clientEmail: String? = null,
        @SerializedName("clientName")
        private val clientName: String? = null,
        private val subject: String,
        private val message: String,
    )

    class Response(
        val status: String? = null,
        val ticketId: Long? = null
    ) : UsedeskApiError()
}