package ru.usedesk.chat_sdk.entity

import android.net.Uri

data class UsedeskFileInfo(
        val uri: Uri,
        val type: String,
        val name: String
) {

    fun isImage(): Boolean {
        return type.startsWith(IMAGE_TYPE)
    }

    fun isVideo(): Boolean {
        return type.startsWith(VIDEO_TYPE)
    }

    companion object {
        const val IMAGE_TYPE = "image/"
        const val VIDEO_TYPE = "video/"
    }
}