package ru.usedesk.knowledgebase_sdk.external.entity

class UsedeskCategory {
    val id: Long = 0
    val title: String? = null

    val public = 0
    val order = 0
    var articles: Array<UsedeskArticleInfo>? = null
}