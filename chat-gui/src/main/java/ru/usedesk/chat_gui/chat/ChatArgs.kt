package ru.usedesk.chat_gui.chat

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal data class ChatArgs(
    val configuration: UsedeskChatConfiguration,
    val agentName: String?,
    val rejectedFileExtensions: List<String>,
    val messagesDateFormat: String,
    val messageTimeFormat: String,
    val adaptiveTextMessageTimePadding: Boolean,
    val groupAgentMessages: Boolean
)