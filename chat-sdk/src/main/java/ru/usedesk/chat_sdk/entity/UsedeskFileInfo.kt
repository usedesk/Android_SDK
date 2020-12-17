package ru.usedesk.chat_sdk.entity

import android.net.Uri

data class UsedeskFileInfo(
        val uri: Uri,
        val type: String,
        val name: String
) {

    fun isImage(): Boolean {
        return Companion.isImage(type)
    }

    fun isVideo(): Boolean {
        return type.startsWith("video/")
    }

    companion object {
        private const val IMAGE_TYPE = "image/"
        private val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "bmp", "png")

        fun isImage(type: String): Boolean {
            return type.startsWith(IMAGE_TYPE) || IMAGE_EXTENSIONS.any {
                type.endsWith(it)
            }
        }
    }
}