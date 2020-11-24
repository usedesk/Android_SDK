package ru.usedesk.chat_sdk.internal.data.repository.configuration

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException
import toothpick.InjectConstructor
import javax.inject.Named

@InjectConstructor
class UserInfoRepository(
        @Named("configuration")
        private val configurationDataLoader: DataLoader<UsedeskChatConfiguration>,
        @Named("token")
        private val tokenDataLoader: DataLoader<String>
) : IUserInfoRepository {

    @Throws(UsedeskDataNotFoundException::class)
    override fun getToken(): String {
        return tokenDataLoader.data
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
        return configurationDataLoader.data
    }

    override fun setConfiguration(configuration: UsedeskChatConfiguration?) {
        if (configuration == null) {
            configurationDataLoader.clearData()
        } else {
            configurationDataLoader.setData(configuration)
        }
    }
}