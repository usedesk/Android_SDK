
package ru.usedesk.common_sdk.api.multipart

import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MultipartBody

interface IUsedeskMultipartConverter {
    fun convert(
        pair: Pair<String, Any?>,
        progressFlow: MutableStateFlow<Pair<Long, Long>>?
    ): MultipartBody.Part?

    class FileBytes(
        val byteArray: ByteArray,
        val originalFile: String
    )
}