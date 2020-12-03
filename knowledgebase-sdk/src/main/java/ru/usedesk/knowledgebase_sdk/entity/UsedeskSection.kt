package ru.usedesk.knowledgebase_sdk.entity

class UsedeskSection {
    val id: Long = 0
    val title: String? = null

    val public = 0
    val order = 0
    val image: String? = null
    var categories: Array<UsedeskCategory>? = null
}