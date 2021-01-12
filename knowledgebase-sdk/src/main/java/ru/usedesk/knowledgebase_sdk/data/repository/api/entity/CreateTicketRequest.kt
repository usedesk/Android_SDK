package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

internal class CreateTicketRequest(
        private val apiToken: String,
        private val subject: String,
        private val message: String
)