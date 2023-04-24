
package ru.usedesk.common_sdk.api.multipart

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toFile
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter.FileBytes
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileName
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getMimeType
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

internal class MultipartConverter @Inject constructor(
    private val contentResolver: ContentResolver
) : IUsedeskMultipartConverter {
    override fun convert(
        pair: Pair<String, Any?>,
        progressFlow: MutableStateFlow<Pair<Long, Long>>?
    ) = when (val value = pair.second) {
        is String -> MultipartBody.Part.createFormData(pair.first, value)
        is Long -> MultipartBody.Part.createFormData(pair.first, value.toString())
        is Uri -> {
            val mimeType = contentResolver.getMimeType(value)
            val mediaType = mimeType.toMediaType()
            val name = contentResolver.getFileName(value)
            //val requestBody = RequestBody.create(mediaType, value.toFile())
            val body = ProgressRequestBody(
                value.toFile(),
                mediaType,
                progressFlow
            )
            MultipartBody.Part.createFormData(pair.first, name, body)
        }
        is FileBytes -> {
            val uri = Uri.parse(value.originalFile)
            val mimeType = contentResolver.getMimeType(uri)
            val mediaType = mimeType.toMediaType()
            val name = contentResolver.getFileName(uri)
            val requestBody = RequestBody.create(mediaType, value.byteArray)
            MultipartBody.Part.createFormData(pair.first, name, requestBody)
        }
        else -> null
    }
}

private class ProgressRequestBody(
    private val file: File,
    private val mediaType: MediaType?,
    private val progressFlow: MutableStateFlow<Pair<Long, Long>>?
) : RequestBody() {
    override fun contentType() = mediaType

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val total = file.length()
        var read: Int
        var uploaded = 0L
        FileInputStream(file).use { inputStream ->
            progressFlow?.value = 0L to total
            while (inputStream.read(buffer).also { read = it } != -1) {
                uploaded += read
                sink.write(buffer, 0, read)
                progressFlow?.value = uploaded to total
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}