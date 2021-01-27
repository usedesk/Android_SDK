package ru.usedesk.knowledgebase_sdk.entity

class UsedeskKnowledgeBaseConfiguration(
        val urlApi: String,
        val accountId: String,
        val token: String,
        val clientEmail: String,
        val clientName: String? = null
)