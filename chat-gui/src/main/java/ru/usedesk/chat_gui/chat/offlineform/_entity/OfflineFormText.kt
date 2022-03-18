package ru.usedesk.chat_gui.chat.offlineform._entity

internal class OfflineFormText(
    key: String,
    title: String,
    required: Boolean,
    val text: String
) : OfflineFormItem(key, title, required)