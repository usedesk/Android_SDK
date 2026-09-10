
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface AddRating {
    class Request(
        @SerializedName("api_token") val apiToken: String,
        @SerializedName("account_id") val accountId: String,
        @SerializedName("article_id") val articleId: Long,
        @SerializedName("count_positive") val countPositive: Int,
        @SerializedName("count_negative") val countNegative: Int
    )

    class Response(
        @SerializedName("rating") val rating: Rating? = null
    ) : UsedeskApiError() {

        class Rating(
            @SerializedName("positive") val positive: Int? = null,
            @SerializedName("negative") val negative: Int? = null
        )
    }
}