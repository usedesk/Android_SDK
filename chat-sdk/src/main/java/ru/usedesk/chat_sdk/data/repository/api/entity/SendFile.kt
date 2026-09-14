
package ru.usedesk.chat_sdk.data.repository.api.entity

import android.net.Uri
import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.UsedeskApiRepository.MultipartRequest
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface SendFile {
    class Request(
        @SerializedName("token") val token: String?,
        @SerializedName("message_id") val messageId: String,
        @SerializedName("file") val file: Uri
    ) : MultipartRequest(
        "chat_token" to token,
        "message_id" to messageId,
        "file" to file
    )

    class Response(
        @SerializedName("status") var status: Int?,
        @SerializedName("file_link") var fileLink: String?,
        @SerializedName("size") var size: String?,
        @SerializedName("id") var id: String?,
        @SerializedName("type") var type: String?,
        @SerializedName("name") var name: String?
    ) : UsedeskApiError()
}