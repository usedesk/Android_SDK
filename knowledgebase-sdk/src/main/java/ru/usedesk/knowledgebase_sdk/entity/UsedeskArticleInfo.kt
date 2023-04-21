
package ru.usedesk.knowledgebase_sdk.entity

data class UsedeskArticleInfo(
    val id: Long,
    val title: String,
    val categoryId: Long,
    val viewsCount: Long
)