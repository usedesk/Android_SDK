package ru.usedesk.common_sdk.di

import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.common_sdk.api.HttpApiFactory
import ru.usedesk.common_sdk.api.IUsedeskHttpApiFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import toothpick.config.Module

class CommonModule(
        appContext: Context
) : Module() {

    init {
        bind(Context::class.java).toInstance(appContext)
        bind(ContentResolver::class.java).toInstance(appContext.contentResolver)
        bind(Scheduler::class.java).withName("io").toInstance(Schedulers.io())
        bind(Scheduler::class.java).withName("main").toInstance(AndroidSchedulers.mainThread())
        bind(Gson::class.java).toInstance(Gson())
        bind(UsedeskOkHttpClientFactory::class.java).toInstance(UsedeskOkHttpClientFactory(appContext))
        bind(IUsedeskHttpApiFactory::class.java).to(HttpApiFactory::class.java).singleton()
    }
}