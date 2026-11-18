package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadPreviousMessages {
    class Request(
        @SerializedName("chat_token") val chatToken: String?,
        @SerializedName("comment_id") val commentId: String
    )

    class Response(
        @SerializedName("items") val items: Array<SocketResponse.AddMessage.Message?>?
    ) : UsedeskApiError()
}