
package ru.usedesk.chat_sdk.entity

internal data class ChatInited(
    val token: String,
    val waitingEmail: Boolean,
    val messages: List<UsedeskMessage>,
    val offlineFormSettings: UsedeskOfflineFormSettings,
    val status: Int?
)