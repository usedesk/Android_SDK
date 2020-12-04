package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import ru.usedesk.chat_sdk.data.repository.api.loader.file.entity.LoadedFile
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import toothpick.InjectConstructor

@InjectConstructor
internal class FileLoader(
        private val contentResolver: ContentResolver
) : IFileLoader {

    override fun load(uri: Uri): LoadedFile {
        contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) {
                throw UsedeskDataNotFoundException("Can't open file: $uri")
            }
            val size = inputStream.available()
            if (size > MAX_FILE_SIZE) {
                throw UsedeskDataNotFoundException("Max file size = $MAX_FILE_SIZE")
            }
            val name: String = getFileName(contentResolver, uri)
            val type = getMimeType(uri)
            val bytes = inputStream.readBytes()
            return LoadedFile(name, size, type, bytes)
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