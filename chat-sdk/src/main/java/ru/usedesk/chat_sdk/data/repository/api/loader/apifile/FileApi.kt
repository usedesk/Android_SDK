package ru.usedesk.chat_sdk.data.repository.api.loader.apifile

import okhttp3.MultipartBody
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.entity.FileResponse
import ru.usedesk.common_sdk.api.IUsedeskHttpApiFactory
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
internal class FileApi(
        private val httpApiFactory: IUsedeskHttpApiFactory
) : IFileApi {

    override fun post(baseUrl: String, request: List<MultipartBody.Part>): FileResponse {
        val httpApi = httpApiFactory.getInstance(baseUrl, IHttpApi::class.java)
        val response = httpApi.postFile(request).execute()
        if (response.isSuccessful) {
            response.body()?.also {
                return it
            }
        }
        throw IOException("Server error: " + response.code())
    }
}