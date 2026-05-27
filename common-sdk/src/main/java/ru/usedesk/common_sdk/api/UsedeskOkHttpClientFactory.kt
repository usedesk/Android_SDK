package ru.usedesk.common_sdk.api

import okhttp3.OkHttpClient

interface UsedeskOkHttpClientFactory {
    fun createInstance(): OkHttpClient
}

@Deprecated(
    message = "Use ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory",
    replaceWith = ReplaceWith(
        "UsedeskOkHttpClientFactory",
        "ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory"
    )
)
typealias IUsedeskOkHttpClientFactory = UsedeskOkHttpClientFactory
