package ru.usedesk.chat_gui.chat.offlineform._entity

internal sealed interface OfflineFormItem {
    val key: String
    val title: String
    val required: Boolean
}