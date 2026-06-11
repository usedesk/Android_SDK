
package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.chat_sdk.entity.ChatInited

internal interface InitChatResponseConverter {
    fun convert(from: SocketResponse.Inited): ChatInited
}