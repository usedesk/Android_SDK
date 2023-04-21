
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

internal class SectionResponse {
    var id: Long? = null
    var title: String? = null
    var public: Long? = null
    var image: String? = null
    var categories: Array<CategoryResponse?>? = null
    var order: Long? = null
}