
package ru.usedesk.common_sdk.api

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

internal class ApiFactory @Inject constructor(
    private val gson: Gson,
    private val okHttpClientFactory: UsedeskOkHttpClientFactory
) : UsedeskApiFactory {

    private val instanceMap: MutableMap<String, Any> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <API> getInstance(baseUrl: String, apiClass: Class<API>): API {
        val url = baseUrl.getCorrectUrl()
        val key = "${apiClass.name}:$url"
        return (instanceMap[key] ?: createInstance(url, apiClass).also {
            instanceMap[key] = it!!
        }) as API
    }

    private fun <API> createInstance(baseUrl: String, apiClass: Class<API>) = Retrofit.Builder()
        .baseUrl(baseUrl.getCorrectUrl())
        .client(okHttpClientFactory.createInstance())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(apiClass)

    private fun String.getCorrectUrl() = trimEnd('/') + '/'
}