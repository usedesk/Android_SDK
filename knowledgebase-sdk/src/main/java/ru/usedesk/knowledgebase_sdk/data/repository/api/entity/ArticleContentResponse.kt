package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class ArticleContentResponse {
    var id: Long? = null
    var title: String? = null
    var text: String? = null

    @SerializedName("category_id")
    var categoryId: String? = null
}