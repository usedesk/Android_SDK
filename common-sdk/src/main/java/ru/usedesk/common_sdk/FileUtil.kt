package ru.usedesk.common_sdk

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

object FileUtil {
    fun getFileName(context: Context, uri: Uri): String {
        return getFileName(context.contentResolver, uri)
    }

    fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri,
                    null,
                    null,
                    null,
                    null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            uri.path?.also {
                val cut = it.lastIndexOf('/')
                result = if (cut >= 0) {
                    it.substring(cut + 1)
                } else {
                    it
                }
            }
        }
        return result ?: ""
    }

    fun getMimeType(context: Context, uri: Uri): String {
        return getMimeType(context.contentResolver, uri)
    }


    fun getMimeType(contentResolver: ContentResolver, uri: Uri): String {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } ?: ""
    }
}