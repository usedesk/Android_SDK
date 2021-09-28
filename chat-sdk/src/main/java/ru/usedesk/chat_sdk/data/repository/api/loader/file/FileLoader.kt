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

    override fun copy(uriSource: Uri, uriDestination: Uri) {
        contentResolver.openInputStream(uriSource).use { inputStream ->
            if (inputStream == null) {
                throw UsedeskDataNotFoundException("Can't read file: $uriSource")
            }
            val size = inputStream.available()
            if (size > MAX_FILE_SIZE) {
                throw UsedeskDataNotFoundException("Max file size = $MAX_FILE_SIZE")
            }
            contentResolver.openOutputStream(uriDestination).use { outputStream ->
                if (outputStream == null) {
                    throw UsedeskDataNotFoundException("Can't write to file: $outputStream")
                }
                val bytes = inputStream.readBytes()
                outputStream.write(bytes)
            }
        }
    }

    companion object {
        private const val MAX_FILE_SIZE = 100 * 1024 * 1024
    }
}