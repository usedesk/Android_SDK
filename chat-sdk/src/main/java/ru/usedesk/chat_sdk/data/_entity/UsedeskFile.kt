package ru.usedesk.chat_sdk.data._entity

import android.webkit.MimeTypeMap
import com.google.gson.Gson
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo

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
        return UsedeskFileInfo.isImage(type)
    }

    fun serialize(): String = Gson().toJson(this)

    companion object {

        fun deserialize(json: String): UsedeskFile = Gson().fromJson(json, UsedeskFile::class.java)
    }
}