package ru.usedesk.knowledgebase_sdk.entity

class UsedeskSearchQuery private constructor(
        val searchQuery: String
) {
    private var collectionIds: List<String>? = null
    private var categoryIds: List<String>? = null
    private var articleIds: List<String>? = null

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

    class Builder(searchQuery: String) {
        private val searchQuery: UsedeskSearchQuery = UsedeskSearchQuery(searchQuery)

        fun setCollectionIds(collectionIds: List<String>?): Builder {
            searchQuery.collectionIds = collectionIds
            return this
        }

        fun setCategoryIds(categoryIds: List<String>?): Builder {
            searchQuery.categoryIds = categoryIds
            return this
        }

        fun setArticleIds(articleIds: List<String>?): Builder {
            searchQuery.articleIds = articleIds
            return this
        }

        fun setCount(count: Int): Builder {
            searchQuery.count = count.toString()
            return this
        }

        fun setPage(page: Int): Builder {
            searchQuery.page = page.toString()
            return this
        }

        fun setType(type: Type?): Builder {
            searchQuery.type = type
            return this
        }

        fun setSort(sort: Sort?): Builder {
            searchQuery.sort = sort
            return this
        }

        fun setOrder(order: Order?): Builder {
            searchQuery.order = order
            return this
        }

        fun build(): UsedeskSearchQuery {
            return searchQuery
        }

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