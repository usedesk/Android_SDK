package ru.usedesk.chat_gui.chat.offlineform._entity

internal data class OfflineFormText(
    override val key: String,
    override val title: String = "",
    override val required: Boolean = false,
    val text: String = ""
) : OfflineFormItem