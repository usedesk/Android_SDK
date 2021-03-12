package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.content.ContentResolver
import android.net.Uri
import ru.usedesk.chat_sdk.data.repository.api.loader.file.entity.LoadedFile
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.utils.UsedeskFileUtil
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
            val name = UsedeskFileUtil.getFileName(contentResolver, uri)
            val type = UsedeskFileUtil.getMimeType(contentResolver, uri)
            val bytes = inputStream.readBytes()
            return LoadedFile(name, size, type, bytes)
        }
    }

    companion object {
        private const val MAX_FILE_SIZE = 100 * 1024 * 1024
    }
}