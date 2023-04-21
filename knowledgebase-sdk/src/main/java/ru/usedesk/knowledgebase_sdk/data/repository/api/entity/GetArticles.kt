
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface GetArticles {
    class Request(
        val query: String,
        type: Type? = null,
        sectionIds: List<Long>? = null,
        categoryIds: List<Long>? = null,
        articleIds: List<Long>? = null,
        sort: Sort? = null,
        order: Order? = null,
        val count: Int? = null,
        val page: Long? = null,
    ) {
        val type = type?.name?.lowercase()
        val sectionIds = sectionIds?.joinToString(",")
        val categoryIds = categoryIds?.joinToString(",")
        val articleIds = articleIds?.joinToString(",")
        val sort = sort?.name?.lowercase()
        val order = order?.name?.lowercase()

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
        val page: Long? = null,
        var count: Long? = null,
        val articles: Array<GetArticleContent.Response?>? = null,
        @SerializedName("last-page")
        val lastPage: Long? = null,
        @SerializedName("total-count")
        val totalCount: Long? = null,
    ) : UsedeskApiError()
}