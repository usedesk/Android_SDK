
package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.UsedeskApiRepository.MultipartRequest
import ru.usedesk.common_sdk.api.entity.UsedeskApiError
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter

internal interface CreateChat {
    class Request(
        apiToken: String,
        companyId: String,
        channelId: String,
        clientName: String?,
        clientEmail: String?,
        clientPhoneNumber: Long?,
        clientAdditionalId: String?,
        clientNote: String?,
        avatar: IUsedeskMultipartConverter.FileBytes?
    ) : MultipartRequest(
        "api_token" to apiToken,
        "company_id" to companyId,
        "channel_id" to channelId,
        "name" to clientName,
        "email" to clientEmail,
        "phone" to clientPhoneNumber,
        "additional_id" to clientAdditionalId,
        "note" to clientNote,
        "avatar" to avatar,
        "platform" to "sdk"
    )

    class Response(
        val status: String? = null,
        val chatId: Long? = null,
        val token: String? = null
    ) : UsedeskApiError()
}