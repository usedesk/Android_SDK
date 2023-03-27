package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface GetArticles {
    class Request(
        val query: String,
        val sectionIds: List<Long>? = null,
        val categoryIds: List<Long>? = null,
        val articleIds: List<Long>? = null,
        val count: Int? = null,
        val page: Long? = null,
        val type: Type? = null,
        val sort: Sort? = null,
        val order: Order? = null
    ) {
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