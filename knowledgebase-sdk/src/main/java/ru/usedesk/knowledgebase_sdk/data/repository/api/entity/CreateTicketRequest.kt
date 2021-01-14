package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class CreateTicketRequest(
        @SerializedName("api_token")
        private val apiToken: String,
        @SerializedName("client_email")
        private val clientEmail: String,
        private val clientName: String? = null,
        message: String,
        articleId: Long
) {
    private val subject = "Отзыв о статье"
    private val tag = "БЗ"
    private val message = "$message\nid $articleId"
}