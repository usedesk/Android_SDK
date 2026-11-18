
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadSections {
    class Request(
        @SerializedName("api_token") val apiToken: String,
        @SerializedName("account_id") val accountId: String
    )

    class Response(
        @SerializedName("items") val items: Array<SectionResponse?>? = null
    ) : UsedeskApiError()
}