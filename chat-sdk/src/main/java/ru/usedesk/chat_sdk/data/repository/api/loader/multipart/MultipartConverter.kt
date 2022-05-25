package ru.usedesk.chat_sdk.data.repository.api.loader.multipart

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileName
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getMimeType
import java.io.File

internal class MultipartConverter(
    private val contentResolver: ContentResolver
) : IMultipartConverter {
    override fun convert(pair: Map.Entry<String, Any?>): MultipartBody.Part? =
        when (val value = pair.value) {
            is String -> MultipartBody.Part.createFormData(pair.key, value)
            is Long -> MultipartBody.Part.createFormData(pair.key, value.toString())
            is Uri -> {
                val mimeType = getMimeType(contentResolver, value)
                val mediaType = MediaType.parse(mimeType)
                val name = getFileName(contentResolver, value)
                val file = File(value.path)
                val requestBody = RequestBody.create(mediaType, file)
                MultipartBody.Part.createFormData(pair.key, name, requestBody)
            }
            else -> null
        }
}