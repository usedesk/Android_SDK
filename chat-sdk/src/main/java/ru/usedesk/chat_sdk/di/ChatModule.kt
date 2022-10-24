package ru.usedesk.chat_sdk.di

import android.content.Context
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.chat_sdk.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.file.FileLoader
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.ConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.MessagesRepository
import ru.usedesk.chat_sdk.domain.CachedMessagesInteractor
import ru.usedesk.chat_sdk.domain.ChatInteractor
import ru.usedesk.chat_sdk.domain.ICachedMessagesInteractor
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.UsedeskCustom
import javax.inject.Scope

@Module(includes = [ChatModuleProvides::class, ChatModuleBinds::class])
internal interface ChatModule

@Module
internal class ChatModuleProvides {
    @[Provides ChatScope]
    fun provideMessagesRepository(
        customMessagesRepository: UsedeskCustom<IUsedeskMessagesRepository>,
        appContext: Context,
        gson: Gson,
        fileLoader: IFileLoader,
        chatConfiguration: UsedeskChatConfiguration,
        messageResponseConverter: MessageResponseConverter
    ): IUsedeskMessagesRepository = customMessagesRepository.customInstance ?: MessagesRepository(
        appContext,
        gson,
        fileLoader,
        chatConfiguration,
        messageResponseConverter
    )
}

@Module
internal interface ChatModuleBinds {
    @[Binds ChatScope]
    fun chatInteractor(interactor: ChatInteractor): IUsedeskChat

    @[Binds ChatScope]
    fun cachedMessagesInteractor(interactor: CachedMessagesInteractor): ICachedMessagesInteractor

    @[Binds ChatScope]
    fun apiRepository(repository: ApiRepository): IApiRepository

    @[Binds ChatScope]
    fun userInfoRepository(repository: UserInfoRepository): IUserInfoRepository

    @[Binds ChatScope]
    fun fileLoader(loader: FileLoader): IFileLoader

    @[Binds ChatScope]
    fun configurationLoader(loader: ConfigurationLoader): IConfigurationLoader
}

@Scope
annotation class ChatScope
