package ru.usedesk.chat_sdk.data.repository.api.loader.multipart

import okhttp3.MultipartBody

internal interface IMultipartConverter {
    fun convert(pair: Map.Entry<String, Any?>): MultipartBody.Part?

    fun convert(
        key: String,
        byteArray: ByteArray,
        originalFile: String
    ): MultipartBody.Part
}