package ru.usedesk.knowledgebase_sdk.entity

import java.util.*

class UsedeskArticleBody(
        val id: Long,
        val title: String,
        val text: String,
        var viewsCount: Long,
        val createdAt: Calendar
)