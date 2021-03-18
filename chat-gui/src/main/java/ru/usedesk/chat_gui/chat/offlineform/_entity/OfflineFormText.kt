package ru.usedesk.chat_gui.chat.offlineform._entity

internal class OfflineFormText(
        title: String,
        required: Boolean,
        val text: String
) : OfflineFormItem(title, required)