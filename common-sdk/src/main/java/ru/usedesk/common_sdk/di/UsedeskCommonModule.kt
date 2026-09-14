
package ru.usedesk.common_sdk.di

import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.common_sdk.api.ApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactoryImpl
import ru.usedesk.common_sdk.api.multipart.UsedeskMultipartConverter
import ru.usedesk.common_sdk.api.multipart.MultipartConverter

@Module(includes = [UsedeskCommonModuleProvides::class, UsedeskCommonModuleBinds::class])
interface UsedeskCommonModule

@Module
internal class UsedeskCommonModuleProvides {
    @Provides
    fun contentResolver(appContext: Context): ContentResolver = appContext.contentResolver

    @Provides
    fun gson(): Gson = Gson()
}

@Module
internal interface UsedeskCommonModuleBinds {
    @Binds
    fun multipartConverter(converter: MultipartConverter): UsedeskMultipartConverter

    @Binds
    fun apiFactory(factory: ApiFactory): UsedeskApiFactory

    @Binds
    fun okHttpClientFactory(factory: UsedeskOkHttpClientFactoryImpl): UsedeskOkHttpClientFactory
}