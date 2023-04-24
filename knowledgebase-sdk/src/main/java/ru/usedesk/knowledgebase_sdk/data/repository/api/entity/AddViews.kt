
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface AddViews {
    class Request(
        val apiToken: String,
        val accountId: String,
        val articleId: Long
    ) {
        val count: Int = 1
    }

    class Response(
        val res: Long? = null,
        val views: Long? = null
    ) : UsedeskApiError()
}