
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

internal class CategoryResponse {
    var id: Long? = null
    var title: String? = null
    var description: String? = null
    var public: Long? = null
    var articles: Array<ArticleInfoResponse?>? = null
    var order: Long? = null
}