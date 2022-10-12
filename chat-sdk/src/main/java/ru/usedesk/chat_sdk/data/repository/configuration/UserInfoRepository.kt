package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class UserInfoRepository @Inject constructor(
    private val configurationLoader: IConfigurationLoader
) : IUserInfoRepository {
    override fun getConfiguration(configuration: UsedeskChatConfiguration) =
        configurationLoader.getData()?.firstOrNull { it.isSameUser(configuration) }

    override fun setConfiguration(configuration: UsedeskChatConfiguration) {
        val configurations = (configurationLoader.getData() ?: arrayOf())
            .filter { !it.isSameUser(configuration) } + configuration
        configurationLoader.setData(configurations.toTypedArray())
    }

    private fun UsedeskChatConfiguration.isSameUser(other: UsedeskChatConfiguration) =
        companyId == other.companyId &&
                channelId == other.channelId &&
                clientEmail == other.clientEmail &&
                clientPhoneNumber == other.clientPhoneNumber &&
                clientName == other.clientName
}