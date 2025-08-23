
package ru.usedesk.chat_sdk.entity

import android.os.Parcelable
import android.webkit.MimeTypeMap
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsedeskFile(
    val content: String,
    val type: String,
    val size: String,
    val name: String
) : Parcelable {
    fun isImage(): Boolean = type.startsWith(UsedeskFileInfo.IMAGE_TYPE)

    fun isVideo(): Boolean = type.startsWith(UsedeskFileInfo.VIDEO_TYPE)

    fun isAudio(): Boolean = type.startsWith(UsedeskFileInfo.AUDIO_TYPE)

    companion object {
        fun create(
            content: String,
            type: String?,
            size: String,
            name: String
        ): UsedeskFile {
            val inputType = type.orEmpty().trim()
            val mimeType = when {
                inputType.contains('/') -> inputType.substringBefore(';').lowercase()
                inputType.isNotEmpty() -> {
                    val ext = inputType.removePrefix(".").lowercase()
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                }
                else -> null
            } ?: run {
                val nameExt = name.substringAfterLast('.', "").lowercase()
                when {
                    nameExt.isNotEmpty() -> MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(nameExt)
                    else -> null
                }
            } ?: ""
            return UsedeskFile(content, mimeType, size, name)
        }
    }
}