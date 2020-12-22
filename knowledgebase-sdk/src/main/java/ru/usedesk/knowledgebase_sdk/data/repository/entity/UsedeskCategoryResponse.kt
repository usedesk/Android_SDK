package ru.usedesk.knowledgebase_sdk.data.repository.entity

internal class UsedeskCategoryResponse {
    val id: Long = 0
    val title: String? = null

    val public = 0
    val order = 0
    var articles: Array<UsedeskArticleInfoResponse>? = null
}