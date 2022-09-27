package ru.usedesk.common_sdk.api.multipart

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toFile
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter.FileBytes
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileName
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getMimeType
import javax.inject.Inject

internal class MultipartConverter @Inject constructor(
    private val contentResolver: ContentResolver
) : IUsedeskMultipartConverter {
    override fun convert(pair: Pair<String, Any?>) = when (val value = pair.second) {
        is String -> MultipartBody.Part.createFormData(pair.first, value)
        is Long -> MultipartBody.Part.createFormData(pair.first, value.toString())
        is Uri -> {
            val mimeType = contentResolver.getMimeType(value)
            val mediaType = MediaType.parse(mimeType)
            val name = contentResolver.getFileName(value)
            val requestBody = RequestBody.create(mediaType, value.toFile())
            MultipartBody.Part.createFormData(pair.first, name, requestBody)
        }
        is FileBytes -> {
            val uri = Uri.parse(value.originalFile)
            val mimeType = contentResolver.getMimeType(uri)
            val mediaType = MediaType.parse(mimeType)
            val name = contentResolver.getFileName(uri)
            val requestBody = RequestBody.create(mediaType, value.byteArray)
            MultipartBody.Part.createFormData(pair.first, name, requestBody)
        }
        else -> null
    }
}