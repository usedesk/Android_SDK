
package ru.usedesk.chat_sdk.entity

import android.content.Context
import android.net.Uri
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileName
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getMimeType

data class UsedeskFileInfo(
    val uri: Uri,
    val type: String,
    val name: String
) {

    fun isImage(): Boolean = type.startsWith(IMAGE_TYPE)

    fun isVideo(): Boolean = type.startsWith(VIDEO_TYPE)

    fun isAudio(): Boolean = type.startsWith(AUDIO_TYPE)

    companion object {
        const val IMAGE_TYPE = "image/"
        const val VIDEO_TYPE = "video/"
        const val AUDIO_TYPE = "audio/"

        fun create(context: Context, uri: Uri): UsedeskFileInfo {
            val mimeType = context.getMimeType(uri)
            val name = context.getFileName(uri)
            return UsedeskFileInfo(uri, mimeType, name)
        }
    }
}