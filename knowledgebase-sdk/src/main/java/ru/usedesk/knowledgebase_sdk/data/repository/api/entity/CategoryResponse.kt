
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class CategoryResponse {
    @SerializedName("id") var id: Long? = null
    @SerializedName("title") var title: String? = null
    @SerializedName("description") var description: String? = null
    @SerializedName("public") var public: Long? = null
    @SerializedName("articles") var articles: Array<ArticleInfoResponse?>? = null
    @SerializedName("order") var order: Long? = null
}