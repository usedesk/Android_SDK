package ru.usedesk.common_sdk.api

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

internal class ApiFactory(
    private val gson: Gson,
    private val okHttpClientFactory: UsedeskOkHttpClientFactory
) : IUsedeskApiFactory {

    private val instanceMap: MutableMap<String, Any> = HashMap()

    override fun <API> getInstance(baseUrl: String, apiClass: Class<API>): API {
        val url = getCorrectUrl(baseUrl)
        val key = "${apiClass.name}:$url"
        return (instanceMap[key] ?: createInstance(url, apiClass).also {
            instanceMap[key] = it!!
        }) as API
    }

    private fun <API> createInstance(baseUrl: String, apiClass: Class<API>): API {
        val url = getCorrectUrl(baseUrl)
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClientFactory.createInstance())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(apiClass)
    }

    private fun getCorrectUrl(url: String): String {
        return url.trimEnd('/') + '/'
    }
}