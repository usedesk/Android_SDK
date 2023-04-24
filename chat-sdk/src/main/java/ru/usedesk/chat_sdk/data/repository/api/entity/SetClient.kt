
package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.UsedeskApiRepository.MultipartRequest
import ru.usedesk.common_sdk.api.entity.UsedeskApiError
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter.FileBytes

internal interface SetClient {
    class Request(
        token: String?,
        companyId: String,
        email: String?,
        userName: String?,
        note: String?,
        phone: Long?,
        additionalId: String?,
        avatar: FileBytes?
    ) : MultipartRequest(
        "token" to token,
        "company_id" to companyId,
        "email" to email,
        "username" to userName,
        "note" to note,
        "phone" to phone,
        "additional_id" to additionalId,
        "avatar" to avatar
    )

    class Response(
        var clientId: Long?
    ) : UsedeskApiError()
}