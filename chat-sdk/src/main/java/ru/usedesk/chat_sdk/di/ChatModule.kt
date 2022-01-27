package ru.usedesk.chat_sdk.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import ru.usedesk.chat_sdk.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.loader.InitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.file.FileLoader
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.data.repository.api.loader.multipart.IMultipartConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.multipart.MultipartConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket.SocketApi
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.ConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.ITokenLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.TokenLoader
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.MessagesRepository
import ru.usedesk.chat_sdk.domain.CachedMessagesInteractor
import ru.usedesk.chat_sdk.domain.ChatInteractor
import ru.usedesk.chat_sdk.domain.ICachedMessagesInteractor
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.di.UsedeskCustom
import javax.inject.Scope

@Module
internal object ChatModule {

    @[Provides ChatScope]
    fun provideSocketApi(
        gson: Gson,
        usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory
    ): SocketApi {
        return SocketApi(gson, usedeskOkHttpClientFactory)
    }

    @[Provides ChatScope]
    fun provideConfigurationLoader(appContext: Context, gson: Gson): IConfigurationLoader {
        return ConfigurationLoader(appContext, gson)
    }

    @[Provides ChatScope]
    fun provideTokenLoader(appContext: Context): ITokenLoader {
        return TokenLoader(appContext)
    }

    @[Provides ChatScope]
    fun provideFileLoader(appContext: Context): IFileLoader {
        return FileLoader(appContext)
    }

    @[Provides ChatScope]
    fun provideMultipartConverter(): IMultipartConverter = MultipartConverter()

    @[Provides ChatScope]
    fun provideMessagesRepository(
        customMessagesRepository: UsedeskCustom<IUsedeskMessagesRepository>,
        appContext: Context,
        gson: Gson,
        fileLoader: IFileLoader,
        chatConfiguration: UsedeskChatConfiguration
    ): IUsedeskMessagesRepository {
        return customMessagesRepository.customInstance ?: MessagesRepository(
            appContext,
            gson,
            fileLoader,
            chatConfiguration
        )
    }

    @[Provides ChatScope]
    fun provideUserInfoRepository(
        configurationLoader: IConfigurationLoader,
        tokenLoader: ITokenLoader
    ): IUserInfoRepository {
        return UserInfoRepository(
            configurationLoader,
            tokenLoader
        )
    }

    @[Provides ChatScope]
    fun provideMessageResponseConverter(): MessageResponseConverter {
        return MessageResponseConverter()
    }

    @[Provides ChatScope]
    fun provideInitChatResponseConverter(
        messageResponseConverter: MessageResponseConverter
    ): InitChatResponseConverter {
        return InitChatResponseConverter(messageResponseConverter)
    }

    @[Provides ChatScope]
    fun provideApiRepository(
        socketApi: SocketApi,
        multipartConverter: IMultipartConverter,
        initChatResponseConverter: InitChatResponseConverter,
        messageResponseConverter: MessageResponseConverter,
        fileLoader: IFileLoader,
        apiFactory: IUsedeskApiFactory,
        gson: Gson
    ): IApiRepository {
        return ApiRepository(
            socketApi,
            multipartConverter,
            initChatResponseConverter,
            messageResponseConverter,
            fileLoader,
            apiFactory,
            gson
        )
    }

    @[Provides ChatScope]
    fun provideCachedMessagesInteractor(
        configuration: UsedeskChatConfiguration,
        messagesRepository: IUsedeskMessagesRepository,
        userInfoRepository: IUserInfoRepository
    ): ICachedMessagesInteractor {
        return CachedMessagesInteractor(
            configuration,
            messagesRepository,
            userInfoRepository
        )
    }

    @[Provides ChatScope]
    fun provideChatInteractor(
        configuration: UsedeskChatConfiguration,
        userInfoRepository: IUserInfoRepository,
        apiRepository: IApiRepository,
        cachedMessagesInteractor: ICachedMessagesInteractor
    ): IUsedeskChat {
        return ChatInteractor(
            configuration,
            userInfoRepository,
            apiRepository,
            cachedMessagesInteractor
        )
    }
}

@Scope
annotation class ChatScope
