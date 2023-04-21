
package ru.usedesk.chat_sdk.data.repository.api.entity

import android.net.Uri
import ru.usedesk.common_sdk.api.UsedeskApiRepository.MultipartRequest
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SendFile {
    class Request(
        val token: String,
        val messageId: Long,
        val file: Uri
    ) : MultipartRequest(
        "chat_token" to token,
        "message_id" to messageId,
        "file" to file
    )

    class Response(
        var status: Int?,
        var fileLink: String?,
        var size: String?,
        var id: String?,
        var type: String?,
        var name: String?
    ) : UsedeskApiError()
}