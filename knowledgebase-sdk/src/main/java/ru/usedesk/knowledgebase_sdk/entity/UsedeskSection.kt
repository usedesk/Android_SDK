
package ru.usedesk.knowledgebase_sdk.entity

data class UsedeskSection(
    val id: Long,
    val title: String,
    val thumbnail: String? = null,
    val categories: List<UsedeskCategory>
)