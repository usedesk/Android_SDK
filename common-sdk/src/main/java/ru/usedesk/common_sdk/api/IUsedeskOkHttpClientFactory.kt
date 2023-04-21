
package ru.usedesk.common_sdk.api

import okhttp3.OkHttpClient

interface IUsedeskOkHttpClientFactory {
    fun createInstance(): OkHttpClient
}