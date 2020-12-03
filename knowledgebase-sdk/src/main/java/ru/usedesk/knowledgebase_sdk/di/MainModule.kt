package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.ApiLoader
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.ApiRetrofit
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.IApiLoader
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.RetrofitEnumConverterFactory
import ru.usedesk.knowledgebase_sdk.data.repository.ApiRepository
import ru.usedesk.knowledgebase_sdk.data.repository.IKnowledgeBaseRepository
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.domain.KnowledgeBaseInteractor
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import toothpick.config.Module

internal class MainModule(
        appContext: Context,
        knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
) : Module() {

    init {
        bind(Context::class.java).toInstance(appContext)
        bind(UsedeskKnowledgeBaseConfiguration::class.java).toInstance(knowledgeBaseConfiguration)
        bind(Gson::class.java).toInstance(gson())
        bind(IUsedeskKnowledgeBase::class.java).to(KnowledgeBaseInteractor::class.java)
        bind(IKnowledgeBaseRepository::class.java).to(ApiRepository::class.java)
        bind(IApiLoader::class.java).to(ApiLoader::class.java)
        bind(ApiRetrofit::class.java).toInstance(apiRetrofit(retrofit()))
        bind(Scheduler::class.java).withName("work").toInstance(Schedulers.io())
        bind(Scheduler::class.java).withName("main").toInstance(AndroidSchedulers.mainThread())
    }

    private fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    private fun retrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(SERVER_BASE_URL)
                .client(okHttpClient())
                .addConverterFactory(scalarsConverterFactory())
                .addConverterFactory(retrofitEnumConverterFactory())
                .build()
    }

    private fun retrofitEnumConverterFactory(): RetrofitEnumConverterFactory {
        return RetrofitEnumConverterFactory()
    }

    private fun scalarsConverterFactory(): ScalarsConverterFactory {
        return ScalarsConverterFactory.create()
    }

    private fun apiRetrofit(retrofit: Retrofit): ApiRetrofit {
        return retrofit.create(ApiRetrofit::class.java)
    }

    private fun gson(): Gson {
        return Gson()
    }

    companion object {
        private const val SERVER_BASE_URL = "https://api.usedesk.ru/support/"
    }
}