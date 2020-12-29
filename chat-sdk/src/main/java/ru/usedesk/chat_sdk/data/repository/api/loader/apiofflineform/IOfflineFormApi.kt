package ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform

import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.entity.OfflineFormRequest

internal interface IOfflineFormApi {
    fun post(baseUrl: String, request: OfflineFormRequest)
}