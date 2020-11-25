package ru.usedesk.knowledgebase_sdk.external.entity

import com.google.gson.annotations.SerializedName

class UsedeskCategory {
    val id: Long = 0
    val title: String? = null

    @SerializedName("public")
    val access = 0
    val order = 0
    var articles: Array<UsedeskArticleInfo>? = null
}