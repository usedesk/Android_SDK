package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.ITokenLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class UserInfoRepository @Inject constructor(
    private val configurationLoader: IConfigurationLoader,
    private val tokenLoader: ITokenLoader
) : IUserInfoRepository {
    override fun getConfiguration(configuration: UsedeskChatConfiguration): UsedeskChatConfiguration? {
        configurationLoader.initLegacyData(tokenLoader::getData)
        val configurations = configurationLoader.getData()
        return configurations?.firstOrNull { it.userKey == configuration.userKey }
    }

    override fun setConfiguration(configuration: UsedeskChatConfiguration) {
        val configurations = configurationLoader.getData() ?: arrayOf()
        val newConfigurations = when {
            configurations.any { it.userKey == configuration.userKey } -> configurations.map {
                when (configuration.userKey) {
                    it.userKey -> configuration
                    else -> it
                }
            }.toTypedArray()
            else -> configurations + configuration
        }
        configurationLoader.setData(newConfigurations)
    }
}