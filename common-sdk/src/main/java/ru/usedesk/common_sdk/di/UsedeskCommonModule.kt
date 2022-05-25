package ru.usedesk.common_sdk.di

import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import ru.usedesk.common_sdk.api.ApiFactory
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory

@Module
object UsedeskCommonModule {

    @Provides
    fun provideContentResolver(appContext: Context): ContentResolver = appContext.contentResolver

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    fun provideUsedeskOkHttpClientFactory(appContext: Context): UsedeskOkHttpClientFactory =
        UsedeskOkHttpClientFactory(appContext)

    @Provides
    fun provideUsedeskApiFactory(
        gson: Gson,
        usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory
    ): IUsedeskApiFactory = ApiFactory(gson, usedeskOkHttpClientFactory)
}