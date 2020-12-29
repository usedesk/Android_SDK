package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.chat_sdk.entity.UsedeskMessage

data class ChatInited(
        val token: String,
        val noOperators: Boolean,
        val waitingEmail: Boolean,
        val messages: List<UsedeskMessage>
)