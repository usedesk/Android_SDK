
package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileName
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

internal class FileLoader @Inject constructor(
    appContext: Context
) : IFileLoader {

    private val contentResolver = appContext.contentResolver
    private val cacheDir = appContext.cacheDir

    override suspend fun save(uri: Uri) = when {
        uri.toString().startsWith("file://" + cacheDir.absolutePath) -> uri
        else -> {
            var outputUri: Uri? = null
            contentResolver.openInputStream(uri).use { inputStream ->
                if (inputStream == null) {
                    throw UsedeskDataNotFoundException("Can't read file: $uri")
                }

                val fileName = contentResolver.getFileName(uri)
                val name = "${System.currentTimeMillis()}${fileName.hashCode()}"
                val newFileName = fileName.replaceBeforeLast(
                    '.',
                    name,
                    missingDelimiterValue = name
                )
                val outputFile = File(cacheDir, newFileName)
                FileOutputStream(outputFile).use(inputStream::copyTo)

                outputUri = Uri.fromFile(outputFile)
            }
            outputUri ?: throw RuntimeException("Something wrong with caching file")
        }
    }

    override suspend fun remove(cachedUri: Uri) {
        try {
            val cachedFile = cachedUri.toFile()
            if (cachedFile.exists()) {
                cachedFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}