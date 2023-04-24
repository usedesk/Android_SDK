
package ru.usedesk.common_sdk.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

object UsedeskFileUtil {
    fun Context.getFileName(uri: Uri) = contentResolver.getFileName(uri)

    fun Context.getFileSize(uri: Uri) = contentResolver.getFileSize(uri)

    fun Context.getMimeType(uri: Uri) = contentResolver.getMimeType(uri)

    fun ContentResolver.getFileSize(uri: Uri): Long {
        var size = -1L
        try {
            when (uri.scheme) {
                ContentResolver.SCHEME_CONTENT -> query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                        size = cursor.getLong(columnIndex)
                    }
                }
                ContentResolver.SCHEME_FILE -> openAssetFileDescriptor(
                    uri,
                    "r"
                )?.use {
                    size = it.length
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    fun ContentResolver.getFileName(uri: Uri): String {
        var name: String? = null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        name = cursor.getString(index)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (name == null) {
            try {
                uri.path?.also {
                    val cut = it.lastIndexOf('/')
                    name = when {
                        cut >= 0 -> it.substring(cut + 1)
                        else -> it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return name ?: ""
    }

    fun ContentResolver.getMimeType(uri: Uri) = try {
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> getType(uri)
            else -> {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } ?: ""
}