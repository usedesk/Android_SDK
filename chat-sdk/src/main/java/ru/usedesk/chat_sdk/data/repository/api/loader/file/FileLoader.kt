package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.content.Context
import android.net.Uri
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.utils.UsedeskFileUtil
import java.io.File
import java.io.FileOutputStream

internal class FileLoader(
    appContext: Context
) : IFileLoader {

    private val contentResolver = appContext.contentResolver
    private val cacheDir = appContext.cacheDir

    override fun toCache(inputUri: Uri): Uri {
        var outputUri: Uri? = null
        contentResolver.openInputStream(inputUri).use { inputStream ->
            if (inputStream == null) {
                throw UsedeskDataNotFoundException("Can't read file: $inputUri")
            }

            val fileName = UsedeskFileUtil.getFileName(contentResolver, inputUri)
            val outputFile = File(cacheDir, fileName)
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            outputUri = Uri.fromFile(outputFile)
        }
        return outputUri ?: throw RuntimeException("Something wrong with caching file")
    }
}