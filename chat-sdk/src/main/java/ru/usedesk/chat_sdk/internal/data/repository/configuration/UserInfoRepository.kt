package ru.usedesk.chat_sdk.internal.data.repository.configuration

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.internal.data.framework.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.internal.data.framework.token.ITokenLoader
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException
import toothpick.InjectConstructor

@InjectConstructor
class UserInfoRepository(
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