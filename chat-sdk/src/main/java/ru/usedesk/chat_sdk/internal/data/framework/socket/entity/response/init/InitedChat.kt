package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.init

import ru.usedesk.chat_sdk.external.entity.chat.UsedeskChatItem

data class InitedChat(
        val token: String,
        val noOperators: Boolean,
        val waitingEmail: Boolean,
        val messages: List<UsedeskChatItem>
)