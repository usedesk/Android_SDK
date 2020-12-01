package ru.usedesk.chat_sdk.internal.data.framework.httpapi

import ru.usedesk.chat_sdk.external.entity.old.UsedeskOfflineForm
import java.io.IOException

interface IHttpApiLoader {
    @Throws(IOException::class)
    fun post(baseUrl: String, offlineForm: UsedeskOfflineForm)
}