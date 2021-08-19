package ru.usedesk.chat_sdk.di

import ru.usedesk.chat_sdk.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
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
import toothpick.config.Module

internal class MainModule(
    usedeskChatConfiguration: UsedeskChatConfiguration,
    usedeskMessagesRepository: IUsedeskMessagesRepository?
) : Module() {

    init {
        bind(UsedeskChatConfiguration::class.java)
            .toInstance(usedeskChatConfiguration)

        bind(SocketApi::class.java)
            .to(SocketApi::class.java)
            .singleton()

        bind(IConfigurationLoader::class.java)
            .to(ConfigurationLoader::class.java)
            .singleton()

        bind(ITokenLoader::class.java)
            .to(TokenLoader::class.java)
            .singleton()

        bind(IFileLoader::class.java)
            .to(FileLoader::class.java)
            .singleton()

        bind(IMultipartConverter::class.java)
            .to(MultipartConverter::class.java)
            .singleton()

        if (usedeskMessagesRepository != null) {
            bind(IUsedeskMessagesRepository::class.java)
                .toInstance(usedeskMessagesRepository)
        } else {
            bind(IUsedeskMessagesRepository::class.java)
                .to(UsedeskMessagesRepository::class.java)
                .singleton()
        }

        bind(IUserInfoRepository::class.java)
            .to(UserInfoRepository::class.java)
            .singleton()

        bind(IApiRepository::class.java)
            .to(ApiRepository::class.java)
            .singleton()


        bind(ICachedMessagesInteractor::class.java)
            .to(CachedMessagesInteractor::class.java)
            .singleton()

        bind(IUsedeskChat::class.java)
            .to(ChatInteractor::class.java)
            .singleton()
    }
}