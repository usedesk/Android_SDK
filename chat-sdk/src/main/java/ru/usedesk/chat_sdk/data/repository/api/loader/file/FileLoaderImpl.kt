
package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import ru.usedesk.common_sdk.api.UsedeskApiRepository.Companion.valueOrNull
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileName
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

internal class FileLoaderImpl @Inject constructor(
    appContext: Context
) : FileLoader {

    private val contentResolver = appContext.contentResolver
    private val cacheDir = appContext.cacheDir

    override suspend fun save(uri: Uri): Uri? = when {
        uri.toString().startsWith("file://" + cacheDir.absolutePath) -> uri
        else -> valueOrNull {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileName = contentResolver.getFileName(uri)
                val name = "${System.currentTimeMillis()}${fileName.hashCode()}"
                val newFileName = fileName.replaceBeforeLast(
                    '.',
                    name,
                    missingDelimiterValue = name
                )
                val outputFile = File(cacheDir, newFileName)
                try {
                    FileOutputStream(outputFile).use(inputStream::copyTo)
                } catch (e: Throwable) {
                    outputFile.delete()
                    throw e
                }
                Uri.fromFile(outputFile)
            }
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