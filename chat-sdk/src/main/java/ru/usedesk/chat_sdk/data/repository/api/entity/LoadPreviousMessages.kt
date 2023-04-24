
package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadPreviousMessages {
    class Request(
        val chatToken: String,
        val commentId: Long
    )

    class Response(
        val items: Array<SocketResponse.AddMessage.Message?>?
    ) : UsedeskApiError()
}