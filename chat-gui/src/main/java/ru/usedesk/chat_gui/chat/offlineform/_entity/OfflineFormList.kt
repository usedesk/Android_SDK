package ru.usedesk.chat_gui.chat.offlineform._entity

internal data class OfflineFormList(
    override val key: String,
    override val title: String,
    override val required: Boolean,
    val items: List<String>,
    val selected: Int
) : OfflineFormItem