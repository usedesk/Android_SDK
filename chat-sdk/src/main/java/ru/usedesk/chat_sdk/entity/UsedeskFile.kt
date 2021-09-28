package ru.usedesk.chat_sdk.entity

import android.webkit.MimeTypeMap

data class UsedeskFile(
    val content: String,
    val type: String,
    val size: String,
    val name: String
) {
    fun isImage(): Boolean {
        return type.startsWith(UsedeskFileInfo.IMAGE_TYPE)
    }

    companion object {
        fun create(
            content: String,
            type: String?,
            size: String,
            name: String
        ): UsedeskFile {
            val mimeType = if (type?.contains('/') == true) {
                type
            } else {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(type ?: "")
            } ?: ""
            return UsedeskFile(content, mimeType, size, name)
        }
    }
}