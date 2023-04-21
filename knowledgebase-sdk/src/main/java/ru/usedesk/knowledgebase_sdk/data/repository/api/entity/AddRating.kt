
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface AddRating {
    class Request(
        val apiToken: String,
        val accountId: String,
        val articleId: Long,
        val countPositive: Int,
        val countNegative: Int
    )

    class Response(
        val rating: Rating? = null
    ) : UsedeskApiError() {

        class Rating(
            val positive: Int? = null,
            val negative: Int? = null
        )
    }
}