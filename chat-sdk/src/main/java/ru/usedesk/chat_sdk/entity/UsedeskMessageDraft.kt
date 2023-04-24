
package ru.usedesk.chat_sdk.entity

data class UsedeskMessageDraft(
    val text: String = "",
    val files: List<UsedeskFileInfo> = listOf()
) {
    val isNotEmpty: Boolean by lazy { text.trim().isNotEmpty() || files.isNotEmpty() }
}