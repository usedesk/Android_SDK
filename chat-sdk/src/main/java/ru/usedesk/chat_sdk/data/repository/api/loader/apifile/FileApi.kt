package ru.usedesk.chat_sdk.data.repository.api.loader.apifile

import okhttp3.MultipartBody
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApiFactory
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.entity.FileResponse
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
internal class FileApi(
        private val httpApiFactory: IHttpApiFactory
) : IFileApi {

    override fun post(baseUrl: String, request: List<MultipartBody.Part>): FileResponse {
        val httpApi = httpApiFactory.getInstance(baseUrl)
        val response = httpApi.postFile(request).execute()
        if (response.isSuccessful) {
            response.body()?.also {
                return it
            }
        }
        throw IOException("Server error: " + response.code())
    }
}