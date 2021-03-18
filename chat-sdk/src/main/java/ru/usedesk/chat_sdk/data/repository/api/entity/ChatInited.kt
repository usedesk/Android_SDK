package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings

internal data class ChatInited(
        val token: String,
        val waitingEmail: Boolean,
        val messages: List<UsedeskMessage>,
        val offlineFormSettings: UsedeskOfflineFormSettings
)