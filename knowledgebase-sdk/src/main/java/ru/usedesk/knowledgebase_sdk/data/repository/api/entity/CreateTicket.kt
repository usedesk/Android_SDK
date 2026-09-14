
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface CreateTicket {
    class Request(
        @SerializedName("api_token") private val apiToken: String,
        @SerializedName("client_email") private val clientEmail: String? = null,
        @SerializedName("clientName")
        private val clientName: String? = null,
        @SerializedName("subject") private val subject: String,
        @SerializedName("message") private val message: String,
    )

    class Response(
        @SerializedName("status") val status: String? = null,
        @SerializedName("ticket_id") val ticketId: Long? = null
    ) : UsedeskApiError()
}