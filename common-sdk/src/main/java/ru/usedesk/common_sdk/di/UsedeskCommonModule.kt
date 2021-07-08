package ru.usedesk.common_sdk.di

import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import ru.usedesk.common_sdk.api.ApiFactory
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import toothpick.config.Module

class UsedeskCommonModule(
    appContext: Context
) : Module() {

    init {
        bind(Context::class.java)
            .toInstance(appContext)

        bind(ContentResolver::class.java)
            .toInstance(appContext.contentResolver)

        bind(Scheduler::class.java)
            //.toInstance(Schedulers.from(Executors.newFixedThreadPool(64)))
            .toInstance(Schedulers.io())

        bind(Gson::class.java)
            .toInstance(GsonBuilder().create())

        bind(UsedeskOkHttpClientFactory::class.java)
            .toInstance(UsedeskOkHttpClientFactory(appContext))

        bind(IUsedeskApiFactory::class.java)
            .to(ApiFactory::class.java)
            .singleton()
    }
}