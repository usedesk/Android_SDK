package ru.usedesk.chat_gui.internal._extra.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import java.util.*

object AttachmentUtils {
    private fun getUriList(data: Intent): List<Uri> {
        var uri = data.data //single file
        val clipData = data.clipData //list of files
        if (clipData != null) {
            val uriList: MutableList<Uri> = ArrayList(clipData.itemCount)
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                uri = item.uri
                if (uri != null) {
                    uriList.add(uri)
                }
            }
            return uriList
        } else if (uri != null) {
            return ArrayList(listOf(uri))
        }
        return ArrayList()
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            context.applicationContext
                    .contentResolver
                    .getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    .toLowerCase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }

    fun getUsedeskFileInfoList(context: Context, data: Intent): List<UsedeskFileInfo> {
        return getUriList(data)
                .map { uri -> createUsedeskFileInfo(context, uri) }
                .toList()
    }

    fun getUsedeskFileInfo(context: Context, uri: Uri): List<UsedeskFileInfo> {
        return listOf(createUsedeskFileInfo(context, uri))
    }

    private fun createUsedeskFileInfo(context: Context, uri: Uri): UsedeskFileInfo {
        val mimeType = getMimeType(context, uri)
        val type = UsedeskFileInfo.Type.getByMimeType(mimeType)
        return UsedeskFileInfo(uri, type)
    }
}