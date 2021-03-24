package ru.usedesk.chat_gui.chat.offlineform._entity

internal class OfflineFormList(
        key: String,
        title: String,
        required: Boolean,
        val items: List<String>,
        val selected: Int
) : OfflineFormItem(key, title, required)