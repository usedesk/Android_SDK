package ru.usedesk.knowledgebase_sdk.external.entity

import com.google.gson.annotations.SerializedName

class UsedeskArticleBody {
    val id: Long = 0
    val title: String? = null
    val text: String? = null

    @SerializedName("public")
    val access = 0
    val order = 0
    val categoryId: Long = 0
    val collectionId: Long = 0
    var views = 0
    val createdAt: String? = null
}