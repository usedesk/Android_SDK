
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface GetArticleContent {
    class Request(
        @SerializedName("account_id") val accountId: String,
        @SerializedName("article_id") val articleId: Long,
        @SerializedName("token") val token: String
    )

    class Response(
        @SerializedName("id") val id: Long? = null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("text") val text: String? = null,
        @SerializedName("category_id") val categoryId: String? = null,
        @SerializedName("views") val views: Long? = null,
        @SerializedName("public") val public: Int? = null
    ) : UsedeskApiError()
}