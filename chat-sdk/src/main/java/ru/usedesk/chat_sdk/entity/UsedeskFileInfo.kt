package ru.usedesk.chat_sdk.entity

import android.net.Uri

data class UsedeskFileInfo(
        val uri: Uri,
        val type: String
) {

    fun isImage(): Boolean {
        return type.startsWith("image/")
    }

    fun isVideo(): Boolean {
        return type.startsWith("video/")
    }
}