package ru.usedesk.chat_sdk._entity

import ru.usedesk.chat_sdk.entity.UsedeskChatItem

data class ChatInited(
        val token: String,
        val noOperators: Boolean,
        val waitingEmail: Boolean,
        val messages: List<UsedeskChatItem>
)