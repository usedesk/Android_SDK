
package ru.usedesk.knowledgebase_sdk.entity

data class UsedeskCategory(
    val id: Long,
    val title: String,
    val description: String,
    val articles: List<UsedeskArticleInfo>
)