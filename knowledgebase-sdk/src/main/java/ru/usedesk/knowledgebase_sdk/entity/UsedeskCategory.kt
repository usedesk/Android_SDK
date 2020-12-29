package ru.usedesk.knowledgebase_sdk.entity

class UsedeskCategory(
        val id: Long,
        val title: String,
        val description: String,
        val articles: List<UsedeskArticleInfo>,
        val order: Long
)