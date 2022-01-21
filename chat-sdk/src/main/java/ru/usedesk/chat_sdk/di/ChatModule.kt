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
import ru.usedesk.chat_sdk.data.repository.messages.UsedeskMessagesRepository
import ru.usedesk.chat_sdk.domain.CachedMessagesInteractor
import ru.usedesk.chat_sdk.domain.ChatInteractor
import ru.usedesk.chat_sdk.domain.ICachedMessagesInteractor
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory

@Module
internal object ChatModule {

    //TODO: appContext
    //TODO: UsedeskChatConfiguration
    //TODO: IUsedeskMessagesRepository

    @Provides
    fun provideSocketApi(
        gson: Gson,
        usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory
    ): SocketApi {
        return SocketApi(gson, usedeskOkHttpClientFactory)
    }

    @Provides
    fun provideConfigurationLoader(appContext: Context, gson: Gson): IConfigurationLoader {
        return ConfigurationLoader(appContext, gson)
    }

    @Provides
    fun provideTokenLoader(appContext: Context): ITokenLoader {
        return TokenLoader(appContext)
    }

    @Provides
    fun provideFileLoader(appContext: Context): IFileLoader {
        return FileLoader(appContext)
    }

    @Provides
    fun provideMultipartConverter(): IMultipartConverter = MultipartConverter()

    @Provides
    fun provideMessagesRepository(
        appContext: Context,
        gson: Gson,
        fileLoader: IFileLoader,
        chatConfiguration: UsedeskChatConfiguration
    ): IUsedeskMessagesRepository {
        return if (true) {
            //TODO: return custom repository
            throw RuntimeException("TODO EPTA")
        } else {
            UsedeskMessagesRepository(appContext, gson, fileLoader, chatConfiguration)
        }
    }

    @Provides
    fun provideUserInfoRepository(
        configurationLoader: IConfigurationLoader,
        tokenLoader: ITokenLoader
    ): IUserInfoRepository {
        return UserInfoRepository(
            configurationLoader,
            tokenLoader
        )
    }

    @Provides
    fun provideMessageResponseConverter(): MessageResponseConverter {
        return MessageResponseConverter()
    }

    @Provides
    fun provideInitChatResponseConverter(
        messageResponseConverter: MessageResponseConverter
    ): InitChatResponseConverter {
        return InitChatResponseConverter(messageResponseConverter)
    }

    @Provides
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

    @Provides
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

    @Provides
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