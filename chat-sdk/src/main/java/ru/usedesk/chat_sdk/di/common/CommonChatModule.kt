
package ru.usedesk.chat_sdk.di.common

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.chat_sdk.data.repository._extra.ChatDatabase
import ru.usedesk.chat_sdk.data.repository._extra.ChatDatabase.Companion.DATABASE_NAME
import ru.usedesk.chat_sdk.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.loader.IInitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.IMessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.InitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.ConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import javax.inject.Scope

@Module(includes = [CommonChatModuleProvides::class, CommonChatModuleBinds::class])
internal interface CommonChatModule

@Module
internal class CommonChatModuleProvides {
    @[Provides CommonChatScope]
    fun chatDatabase(appContext: Context): ChatDatabase = Room.databaseBuilder(
        appContext,
        ChatDatabase::class.java,
        DATABASE_NAME
    ).build()
}

@Module
internal interface CommonChatModuleBinds {
    @[Binds CommonChatScope]
    fun apiRepository(repository: ApiRepository): IApiRepository

    @[Binds CommonChatScope]
    fun userInfoRepository(repository: UserInfoRepository): IUserInfoRepository

    @[Binds CommonChatScope]
    fun configurationLoader(loader: ConfigurationLoader): IConfigurationLoader

    @[Binds CommonChatScope]
    fun messageResponseConverter(loader: MessageResponseConverter): IMessageResponseConverter

    @[Binds CommonChatScope]
    fun initChatResponseConverter(loader: InitChatResponseConverter): IInitChatResponseConverter
}

@Scope
annotation class CommonChatScope
