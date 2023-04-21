
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface GetArticleContent {
    class Request(
        val accountId: String,
        val articleId: Long,
        val token: String
    )

    class Response(
        val id: Long? = null,
        val title: String? = null,
        val text: String? = null,
        val categoryId: String? = null,
        val views: Long? = null,
        val public: Int? = null
    ) : UsedeskApiError()
}