package ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform

import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.entity.OfflineFormRequest
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
internal class OfflineFormApi(
        private val apiFactory: IUsedeskApiFactory
) : IOfflineFormApi {

    override fun post(baseUrl: String, request: OfflineFormRequest) {
        val httpApi = apiFactory.getInstance(baseUrl, IHttpApi::class.java)
        val response = httpApi.postOfflineForm(request).execute()
        if (response.isSuccessful && response.body() != null) {
            return
        }
        throw IOException("Server error: " + response.code())
    }
}