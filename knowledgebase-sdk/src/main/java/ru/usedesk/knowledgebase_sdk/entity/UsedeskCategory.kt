package ru.usedesk.knowledgebase_sdk.entity

import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleInfoOld

class UsedeskCategory(
        val id: Long,
        val title: String,
        val articles: List<UsedeskArticleInfoOld>
)