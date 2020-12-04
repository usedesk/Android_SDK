package ru.usedesk.chat_sdk.data.repository._extra.multipart

import android.net.Uri
import okhttp3.MultipartBody

internal interface IMultipartConverter {
    fun convert(key: String, value: String): MultipartBody.Part

    fun convert(key: String, uri: Uri): MultipartBody.Part
}