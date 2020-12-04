package ru.usedesk.chat_sdk.data.repository._extra.retrofit

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.usedesk.chat_sdk.data.repository._extra.Factory
import toothpick.InjectConstructor

@InjectConstructor
internal class HttpApiFactory(
        private val gson: Gson
) : Factory<String, IHttpApi>(), IHttpApiFactory {

    override fun createInstance(key: String): IHttpApi {
        return Retrofit.Builder()
                .baseUrl(key)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(IHttpApi::class.java)
    }
}