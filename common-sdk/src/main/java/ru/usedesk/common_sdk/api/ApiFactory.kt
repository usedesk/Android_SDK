package ru.usedesk.common_sdk.api

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
internal class ApiFactory(
        private val gson: Gson,
        private val okHttpClientFactory: UsedeskOkHttpClientFactory
) : IUsedeskApiFactory {

    private val instanceMap: MutableMap<String, Any> = HashMap()

    override fun <API> getInstance(baseUrl: String, apiClass: Class<API>): API {
        val key = "${apiClass.name}:$baseUrl"
        return (instanceMap[key] ?: createInstance(baseUrl, apiClass).also {
            instanceMap[key] = it!!
        }) as API
    }

    private fun <API> createInstance(baseUrl: String, apiClass: Class<API>): API {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClientFactory.createInstance())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(apiClass)
    }
}