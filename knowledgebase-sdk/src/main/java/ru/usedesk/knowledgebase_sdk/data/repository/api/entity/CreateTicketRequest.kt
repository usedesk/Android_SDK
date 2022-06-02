package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class CreateTicketRequest(
    private val apiToken: String,
    private val clientEmail: String? = null,
    @SerializedName("clientName")
    private val clientName: String? = null,
    message: String,
    articleId: Long
) {
    private val subject = "Отзыв о статье"
    private val tag = "БЗ"
    private val message = "$message\nid $articleId"
}