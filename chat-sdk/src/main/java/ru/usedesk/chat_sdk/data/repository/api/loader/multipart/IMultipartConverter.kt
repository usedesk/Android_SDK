package ru.usedesk.chat_sdk.data.repository.api.loader.multipart

import okhttp3.MultipartBody
import ru.usedesk.chat_sdk.data.repository.api.loader.file.entity.LoadedFile

internal interface IMultipartConverter {
    fun convert(key: String, value: String): MultipartBody.Part

    fun convert(key: String, value: Long): MultipartBody.Part

    fun convert(key: String, loadedFile: LoadedFile): MultipartBody.Part
}