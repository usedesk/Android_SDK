package ru.usedesk.chat_sdk.internal.data.framework.httpapi

import ru.usedesk.chat_sdk.external.entity.old.UsedeskOfflineForm
import ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit.IHttpApiFactory
import toothpick.InjectConstructor
import java.io.IOException

@InjectConstructor
class HttpApiLoader(
        private val httpApiFactory: IHttpApiFactory
) : IHttpApiLoader {

    @Throws(IOException::class)
    override fun post(baseUrl: String, offlineForm: UsedeskOfflineForm) {
        val httpApi = httpApiFactory.getInstance(baseUrl)
        val response = httpApi.postOfflineForm(offlineForm).execute()
        if (response.isSuccessful && response.body() != null) {
            return
        }
        throw IOException("Server error: " + response.code())
    }
}