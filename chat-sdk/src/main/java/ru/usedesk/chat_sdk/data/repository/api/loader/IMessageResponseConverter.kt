
package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.chat_sdk.entity.UsedeskMessage

internal interface IMessageResponseConverter {
    fun convertText(text: String): String

    fun convert(from: SocketResponse.AddMessage.Message?): List<UsedeskMessage>
}