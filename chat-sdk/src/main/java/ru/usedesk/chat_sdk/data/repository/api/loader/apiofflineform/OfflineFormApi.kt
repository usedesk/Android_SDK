package ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform

import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.entity.OfflineFormRequest
import ru.usedesk.common_sdk.api.IUsedeskHttpApiFactory
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
internal class OfflineFormApi(
        private val httpApiFactory: IUsedeskHttpApiFactory
) : IOfflineFormApi {

    override fun post(baseUrl: String, request: OfflineFormRequest) {
        val httpApi = httpApiFactory.getInstance(baseUrl, IHttpApi::class.java)
        val response = httpApi.postOfflineForm(request).execute()
        if (response.isSuccessful && response.body() != null) {
            return
        }
        throw IOException("Server error: " + response.code())
    }
}