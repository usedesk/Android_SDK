package ru.usedesk.chat_sdk.data.repository.api.loader.multipart

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.usedesk.chat_sdk.data.repository.api.loader.file.entity.LoadedFile
import toothpick.InjectConstructor

@InjectConstructor
internal class MultipartConverter : IMultipartConverter {
    override fun convert(key: String, value: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(key, value)
    }

    override fun convert(
            key: String,
            loadedFile: LoadedFile
    ): MultipartBody.Part {
        val mediaType: MediaType = loadedFile.type.toMediaType()
        val requestBody = loadedFile.bytes.toRequestBody(mediaType, 0, loadedFile.size)
        return MultipartBody.Part.createFormData(key, loadedFile.name, requestBody)
    }
}