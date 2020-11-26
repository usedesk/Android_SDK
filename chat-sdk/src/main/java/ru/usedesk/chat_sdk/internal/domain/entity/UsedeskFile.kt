package ru.usedesk.chat_sdk.internal.domain.entity

import android.webkit.MimeTypeMap
import com.google.gson.Gson

data class UsedeskFile(
        val content: String,
        val type: String,
        val size: String,
        val name: String
) {
    fun isImage(): Boolean {
        val type = if (this.type.isNotEmpty()) {
            this.type
        } else {
            val extension = name.substringAfterLast('.')
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } ?: ""
        return type.startsWith(IMAGE_TYPE)
    }

    fun serialize(): String = Gson().toJson(this)

    companion object {
        private const val IMAGE_TYPE = "image/"

        fun deserialize(json: String): UsedeskFile = Gson().fromJson(json, UsedeskFile::class.java)
    }
}