package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

internal class SearchQueryRequest(
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
        PUBLIC, PRIVATE
    }

    enum class Sort {
        ID, TITLE, CATEGORY_ID, PUBLIC, CREATED_AT
    }

    enum class Order {
        ASCENDING, DESCENDING
    }
}