package ru.usedesk.knowledgebase_sdk.entity

internal class UsedeskSearchQuery private constructor(
        builder: Builder
) {
    val searchQuery: String = builder.searchQuery
    val collectionIds: List<String>? = builder.collectionIds
    val categoryIds: List<String>? = builder.categoryIds
    val articleIds: List<String>? = builder.articleIds
    val count: String? = builder.count
    val page: String? = builder.page
    val type: Type? = builder.type
    val sort: Sort? = builder.sort
    val order: Order? = builder.order

    fun getCollectionIds(): String? {
        return collectionAsString(collectionIds)
    }

    fun getCategoryIds(): String? {
        return collectionAsString(categoryIds)
    }

    fun getArticleIds(): String? {
        return collectionAsString(articleIds)
    }

    enum class Type {
        PUBLIC, PRIVATE
    }

    enum class Sort {
        ID, TITLE, CATEGORY_ID, PUBLIC, CREATED_AT
    }

    enum class Order {
        ASCENDING, DESCENDING
    }

    class Builder(val searchQuery: String) {
        var collectionIds: List<String>? = null
            private set
        var categoryIds: List<String>? = null
            private set
        var articleIds: List<String>? = null
            private set
        var count: String? = null
            private set
        var page: String? = null
            private set
        var type: Type? = null
            private set
        var sort: Sort? = null
            private set
        var order: Order? = null
            private set

        fun collectionIds(collectionIds: List<String>?) = apply { this.collectionIds = collectionIds }
        fun categoryIds(categoryIds: List<String>?) = apply { this.categoryIds = categoryIds }
        fun articleIds(articleIds: List<String>?) = apply { this.articleIds = articleIds }
        fun count(count: String?) = apply { this.count = count }
        fun page(page: String?) = apply { this.page = page }
        fun type(type: Type?) = apply { this.type = type }
        fun sort(sort: Sort?) = apply { this.sort = sort }
        fun order(order: Order?) = apply { this.order = order }

        fun build() = UsedeskSearchQuery(this)
    }

    companion object {
        private fun collectionAsString(list: List<String>?): String? {
            if (list != null && list.isNotEmpty()) {
                val builder = StringBuilder()
                for (item in list) {
                    builder.append(item)
                    builder.append(',')
                }
                builder.deleteCharAt(builder.length - 1)
                return builder.toString()
            }
            return null
        }
    }
}