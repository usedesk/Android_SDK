
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface AddViews {
    class Request(
        @SerializedName("api_token") val apiToken: String,
        @SerializedName("account_id") val accountId: String,
        @SerializedName("article_id") val articleId: Long
    ) {
        @SerializedName("count") val count: Int = 1
    }

    class Response(
        @SerializedName("res") val res: Long? = null,
        @SerializedName("views") val views: Long? = null
    ) : UsedeskApiError()
}