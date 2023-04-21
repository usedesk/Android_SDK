
package ru.usedesk.knowledgebase_sdk.entity

data class UsedeskArticleContent(
    val id: Long,
    val title: String,
    val categoryId: Long,
    val viewsCount: Long,
    val text: String,
    val public: Boolean
)