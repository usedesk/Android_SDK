
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class SectionResponse {
    @SerializedName("id") var id: Long? = null
    @SerializedName("title") var title: String? = null
    @SerializedName("public") var public: Long? = null
    @SerializedName("image") var image: String? = null
    @SerializedName("categories") var categories: Array<CategoryResponse?>? = null
    @SerializedName("order") var order: Long? = null
}