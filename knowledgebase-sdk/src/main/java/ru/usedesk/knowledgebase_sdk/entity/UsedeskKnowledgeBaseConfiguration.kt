package ru.usedesk.knowledgebase_sdk.entity

class UsedeskKnowledgeBaseConfiguration @JvmOverloads constructor(
        val urlApi: String = "https://api.usedesk.ru/",
        val accountId: String,
        val token: String,
        val clientEmail: String,
        val clientName: String? = null
)