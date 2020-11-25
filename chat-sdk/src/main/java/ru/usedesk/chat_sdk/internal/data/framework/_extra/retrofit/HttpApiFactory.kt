package ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApi
import toothpick.InjectConstructor

@InjectConstructor
class HttpApiFactory(
        private val gson: Gson
) : Factory<String, IHttpApi>(), IHttpApiFactory {

    override fun createInstance(baseUrl: String): IHttpApi {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(IHttpApi::class.java)
    }
}