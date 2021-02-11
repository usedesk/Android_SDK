package ru.usedesk.chat_sdk.entity

import android.content.Context
import android.net.Uri
import ru.usedesk.common_sdk.utils.UsedeskFileUtil

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

        fun create(context: Context, uri: Uri): UsedeskFileInfo {
            val mimeType = UsedeskFileUtil.getMimeType(context, uri)
            val name = UsedeskFileUtil.getFileName(context, uri)
            return UsedeskFileInfo(uri, mimeType, name)
        }
    }
}