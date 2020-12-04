package ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform

import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApiFactory
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.entity.OfflineFormRequest
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
internal class OfflineFormApi(
        private val httpApiFactory: IHttpApiFactory
) : IOfflineFormApi {

    override fun post(baseUrl: String, request: OfflineFormRequest) {
        val httpApi = httpApiFactory.getInstance(baseUrl)
        val response = httpApi.postOfflineForm(request).execute()
        if (response.isSuccessful && response.body() != null) {
            return
        }
        throw IOException("Server error: " + response.code())
    }
}