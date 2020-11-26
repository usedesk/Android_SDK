package ru.usedesk.chat_gui.internal._extra

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo

private fun getUriList(data: Intent): List<Uri> {
    val uri = data.data //single file
    val clipData = data.clipData //list of files
    if (clipData != null) {
        return (0 until clipData.itemCount).mapNotNull { i ->
            clipData.getItemAt(i).uri
        }
    } else if (uri != null) {
        return listOf(uri)
    }
    return listOf()
}

private fun getMimeType(context: Context, uri: Uri): String? {
    return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        context.applicationContext.contentResolver.getType(uri)
    } else {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase()
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

fun getUsedeskFileInfoList(context: Context, data: Intent): List<UsedeskFileInfo> {
    return getUriList(data).map {
        createUsedeskFileInfo(context, it)
    }
}

fun getUsedeskFileInfo(context: Context, uri: Uri): List<UsedeskFileInfo> {
    return listOf(createUsedeskFileInfo(context, uri))
}

private fun createUsedeskFileInfo(context: Context, uri: Uri): UsedeskFileInfo {
    val mimeType = getMimeType(context, uri)
    val type = UsedeskFileInfo.Type.getByMimeType(mimeType)
    return UsedeskFileInfo(uri, type)
}