package ru.usedesk.chat_sdk.entity

data class UsedeskOfflineForm(
        val clientName: String,
        val clientEmail: String,
        val subject: String,
        val additionalFields: List<String>,
        val message: String
)