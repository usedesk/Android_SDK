package ru.usedesk.chat_sdk.data.framework.httpapi

import ru.usedesk.chat_sdk.data.framework._extra.retrofit.IHttpApiFactory
import ru.usedesk.chat_sdk.data.repository.api._entity.request.OfflineFormRequest
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
class HttpApiLoader(
        private val httpApiFactory: IHttpApiFactory
) : IHttpApiLoader {

    @Throws(IOException::class)
    override fun post(baseUrl: String, request: OfflineFormRequest) {
        val httpApi = httpApiFactory.getInstance(baseUrl)
        val response = httpApi.postOfflineForm(request).execute()
        if (response.isSuccessful && response.body() != null) {
            return
        }
        throw IOException("Server error: " + response.code())
    }
}