package ru.usedesk.common_sdk.di

import android.content.ContentResolver
import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.common_sdk.api.ApiFactory
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.api.multipart.MultipartConverter

@Module(includes = [UsedeskCommonModuleProvides::class, UsedeskCommonModuleBinds::class])
interface UsedeskCommonModule

@Module
internal class UsedeskCommonModuleProvides {
    @Provides
    fun contentResolver(appContext: Context): ContentResolver = appContext.contentResolver

    @Provides
    fun gson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
}

@Module
internal interface UsedeskCommonModuleBinds {
    @Binds
    fun dateConverter(converter: MultipartConverter): IUsedeskMultipartConverter

    /*@Binds
    fun okHttpClientFactory(factory: UsedeskOkHttpClientFactory): UsedeskOkHttpClientFactory*/

    @Binds
    fun apiFactory(factory: ApiFactory): IUsedeskApiFactory
}