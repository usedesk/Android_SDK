package ru.usedesk.chat_sdk.data.repository._extra.multipart

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import toothpick.InjectConstructor

@InjectConstructor
internal class MultipartConverter(
        private val contentResolver: ContentResolver
) : IMultipartConverter {
    override fun convert(key: String, value: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(key, value)
    }

    override fun convert(
            key: String,
            uri: Uri
    ): MultipartBody.Part {
        contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) {
                throw UsedeskDataNotFoundException("Can't open file: $uri")
            }
            val size = inputStream.available()
            if (size > MAX_FILE_SIZE) {
                throw UsedeskDataNotFoundException("Max file size = $MAX_FILE_SIZE")
            }
            val bytes = inputStream.readBytes()
            val mediaType: MediaType = getMimeType(uri).toMediaType()
            val requestBody = bytes.toRequestBody(mediaType, 0, size)
            val fileName: String = getFileName(contentResolver, uri)
            return MultipartBody.Part.createFormData(key, fileName, requestBody)
        }
    }

    private fun getMimeType(uri: Uri): String {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } ?: ""
    }

    private fun getFileName(contentResolver: ContentResolver,
                            uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri,
                    null,
                    null,
                    null,
                    null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result!!
    }

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024
    }
}