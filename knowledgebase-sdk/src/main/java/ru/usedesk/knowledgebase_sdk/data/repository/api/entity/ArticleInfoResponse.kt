
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class ArticleInfoResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("views") val views: Long? = null
)