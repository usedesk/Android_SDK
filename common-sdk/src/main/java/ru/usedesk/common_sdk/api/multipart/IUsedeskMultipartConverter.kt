package ru.usedesk.common_sdk.api.multipart

import okhttp3.MultipartBody

interface IUsedeskMultipartConverter {
    fun convert(pair: Pair<String, Any?>): MultipartBody.Part?

    class FileBytes(
        val byteArray: ByteArray,
        val originalFile: String
    )
}