package ru.usedesk.chat_sdk.data.repository._extra.multipart

import android.net.Uri
import okhttp3.MultipartBody
import toothpick.InjectConstructor

@InjectConstructor
internal interface IMultipartConverter {
    fun makePart(key: String, value: String): MultipartBody.Part

    fun makePart(key: String, uri: Uri): MultipartBody.Part
}