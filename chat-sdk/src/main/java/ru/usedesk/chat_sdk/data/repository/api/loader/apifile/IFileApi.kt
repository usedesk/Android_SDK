package ru.usedesk.chat_sdk.data.repository.api.loader.apifile

import okhttp3.MultipartBody
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.entity.FileResponse

internal interface IFileApi {
    fun post(baseUrl: String, request: List<MultipartBody.Part>): FileResponse
}