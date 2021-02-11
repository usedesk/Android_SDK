package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.ITokenLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import toothpick.InjectConstructor

@InjectConstructor
internal class UserInfoRepository(
        private val configurationDataLoader: IConfigurationLoader,
        private val tokenDataLoader: ITokenLoader
) : IUserInfoRepository {

    @Throws(UsedeskDataNotFoundException::class)
    override fun getToken(): String {
        return tokenDataLoader.getData()
    }

    override fun setToken(token: String?) {
        if (token == null) {
            tokenDataLoader.clearData()
        } else {
            tokenDataLoader.setData(token)
        }
    }

    @Throws(UsedeskDataNotFoundException::class)
    override fun getConfiguration(): UsedeskChatConfiguration {
        return configurationDataLoader.getData()
    }

    override fun setConfiguration(configuration: UsedeskChatConfiguration?) {
        if (configuration == null) {
            configurationDataLoader.clearData()
        } else {
            configurationDataLoader.setData(configuration)
        }
    }
}