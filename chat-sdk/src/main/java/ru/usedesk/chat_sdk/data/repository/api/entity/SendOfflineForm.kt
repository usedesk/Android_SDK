
package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SendOfflineForm {
    class Request(
        val email: String,
        val name: String,
        val companyId: String,
        val message: String,
        val topic: String,
        jsonFields: List<Pair<String, String?>>
    ) : UsedeskApiRepository.JsonRequest(jsonFields)

    class Response(
        val status: Int?
    ) : UsedeskApiError()
}