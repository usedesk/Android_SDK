package ru.usedesk.chat_sdk.internal.data.framework.fileinfo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Base64OutputStream
import android.webkit.MimeTypeMap
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import toothpick.InjectConstructor
import java.io.*

@InjectConstructor
class FileInfoLoader(
        private val context: Context
) : IFileInfoLoader {

    @Throws(IOException::class)
    private fun transferTo(inputStream: InputStream, outputStream: OutputStream): Int {
        var bytesRead: Int
        var offset = 0
        val buffer = ByteArray(BUF_SIZE)
        while (inputStream.read(buffer).also { bytesRead = it } > 0) {
            outputStream.write(buffer, 0, bytesRead)
            offset += bytesRead
        }
        return offset
    }

    @Throws(IOException::class)
    private fun convertToBase64(context: Context, uri: Uri): String {
        context.contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) {
                throw RuntimeException("Can't open file: $uri")
            }
            val size = inputStream.available()
            if (size > MAX_FILE_SIZE) {
                throw RuntimeException("File size is bigger then $MAX_FILE_SIZE")
            }
            val output = ByteArrayOutputStream()
            val output64 = Base64OutputStream(output, Base64.DEFAULT)
            transferTo(inputStream, output64)
            return output.toString()
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            context.applicationContext
                    .contentResolver
                    .getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }

    @Throws(UsedeskException::class)
    override fun getFrom(fileInfo: UsedeskFileInfo): UsedeskFile {
        return try {
            val file = File(fileInfo.uri.path ?: "")
            val base64 = convertToBase64(context, fileInfo.uri)
            val mimeType = getMimeType(fileInfo.uri) ?: ""
            val content = String.format(CONTENT_FORMAT, mimeType, base64)
            val size = ""
            val name = file.name
            UsedeskFile(content, mimeType, size, name)
        } catch (e: IOException) {
            throw UsedeskDataNotFoundException(e.message)
        }
    }

    companion object {
        private const val CONTENT_FORMAT = "data:%1s;base64,%2s"
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024
        private const val BUF_SIZE = 100 * 1024
    }
}