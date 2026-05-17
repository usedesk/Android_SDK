package ru.usedesk.common_sdk.api.multipart

import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MultipartBody

interface UsedeskMultipartConverter {
    fun convert(
        pair: Pair<String, Any?>,
        progressFlow: MutableStateFlow<Pair<Long, Long>>?
    ): MultipartBody.Part?

    class FileBytes(
        val byteArray: ByteArray,
        val originalFile: String
    )
}

@Deprecated(
    message = "Use ru.usedesk.common_sdk.api.multipart.UsedeskMultipartConverter",
    replaceWith = ReplaceWith(
        "UsedeskMultipartConverter",
        "ru.usedesk.common_sdk.api.multipart.UsedeskMultipartConverter"
    )
)
typealias IUsedeskMultipartConverter = UsedeskMultipartConverter
