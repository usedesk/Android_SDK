
package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SendOfflineForm {
    class Request(
        @SerializedName("email") val email: String,
        @SerializedName("name") val name: String,
        @SerializedName("company_id") val companyId: String,
        @SerializedName("message") val message: String,
        @SerializedName("topic") val topic: String,
        jsonFields: List<Pair<String, String?>>
    ) : UsedeskApiRepository.JsonRequest(jsonFields)

    class Response(
        @SerializedName("status") val status: Int?
    ) : UsedeskApiError()
}