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
    ): SocketApi = SocketApi(gson, usedeskOkHttpClientFactory)

    @[Provides ChatScope]
    fun provideConfigurationLoader(
        appContext: Context
    ): IConfigurationLoader = ConfigurationLoader(appContext)

    @[Provides ChatScope]
    fun provideTokenLoader(appContext: Context): ITokenLoader = TokenLoader(appContext)

    @[Provides ChatScope]
    fun provideFileLoader(appContext: Context): IFileLoader = FileLoader(appContext)

    @[Provides ChatScope]
    fun provideMultipartConverter(context: Context): IMultipartConverter =
        MultipartConverter(context.contentResolver)

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

    @[Provides ChatScope]
    fun provideUserInfoRepository(
        configurationLoader: IConfigurationLoader,
        tokenLoader: ITokenLoader
    ): IUserInfoRepository = UserInfoRepository(
        configurationLoader,
        tokenLoader
    )

    @[Provides ChatScope]
    fun provideMessageResponseConverter(): MessageResponseConverter = MessageResponseConverter()

    @[Provides ChatScope]
    fun provideInitChatResponseConverter(
        messageResponseConverter: MessageResponseConverter
    ): InitChatResponseConverter = InitChatResponseConverter(messageResponseConverter)

    @[Provides ChatScope]
    fun provideApiRepository(
        appContext: Context,
        socketApi: SocketApi,
        multipartConverter: IMultipartConverter,
        initChatResponseConverter: InitChatResponseConverter,
        messageResponseConverter: MessageResponseConverter,
        apiFactory: IUsedeskApiFactory,
        gson: Gson
    ): IApiRepository = ApiRepository(
        socketApi,
        multipartConverter,
        initChatResponseConverter,
        messageResponseConverter,
        appContext.contentResolver,
        apiFactory,
        gson
    )

    @[Provides ChatScope]
    fun provideCachedMessagesInteractor(
        configuration: UsedeskChatConfiguration,
        messagesRepository: IUsedeskMessagesRepository,
        userInfoRepository: IUserInfoRepository
    ): ICachedMessagesInteractor = CachedMessagesInteractor(
        configuration,
        messagesRepository,
        userInfoRepository
    )

    @[Provides ChatScope]
    fun provideChatInteractor(
        configuration: UsedeskChatConfiguration,
        userInfoRepository: IUserInfoRepository,
        apiRepository: IApiRepository,
        cachedMessagesInteractor: ICachedMessagesInteractor
    ): IUsedeskChat = ChatInteractor(
        configuration,
        userInfoRepository,
        apiRepository,
        cachedMessagesInteractor
    )
}

@Scope
annotation class ChatScope
