
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface GetArticles {
    class Request(
        @SerializedName("query") val query: String,
        type: Type? = null,
        sectionIds: List<Long>? = null,
        categoryIds: List<Long>? = null,
        articleIds: List<Long>? = null,
        sort: Sort? = null,
        order: Order? = null,
        @SerializedName("count") val count: Int? = null,
        @SerializedName("page") val page: Long? = null,
    ) {
        @SerializedName("type") val type = type?.name?.lowercase()
        @SerializedName("section_ids") val sectionIds = sectionIds?.joinToString(",")
        @SerializedName("category_ids") val categoryIds = categoryIds?.joinToString(",")
        @SerializedName("article_ids") val articleIds = articleIds?.joinToString(",")
        @SerializedName("sort") val sort = sort?.name?.lowercase()
        @SerializedName("order") val order = order?.name?.lowercase()

        enum class Type {
            PUBLIC,
            PRIVATE
        }

        enum class Sort {
            ID,
            TITLE,
            CATEGORY_ID,
            PUBLIC,
            CREATED_AT
        }

        enum class Order {
            ASCENDING,
            DESCENDING
        }
    }

    class Response(
        @SerializedName("page") val page: Long? = null,
        @SerializedName("count") var count: Long? = null,
        @SerializedName("articles") val articles: Array<GetArticleContent.Response?>? = null,
        @SerializedName("last-page")
        val lastPage: Long? = null,
        @SerializedName("total-count")
        val totalCount: Long? = null,
    ) : UsedeskApiError()
}