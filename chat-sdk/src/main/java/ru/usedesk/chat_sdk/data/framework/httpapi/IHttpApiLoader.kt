package ru.usedesk.chat_sdk.data.framework.httpapi

import ru.usedesk.chat_sdk.data.repository.api._entity.request.OfflineFormRequest
import java.io.IOException

interface IHttpApiLoader {
    @Throws(IOException::class)
    fun post(baseUrl: String, request: OfflineFormRequest)
}