package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.ITokenLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal class UserInfoRepository(
    private val configurationLoader: IConfigurationLoader,
    private val tokenLoader: ITokenLoader
) : IUserInfoRepository {
    override fun getConfiguration(
        configuration: UsedeskChatConfiguration
    ): UsedeskChatConfiguration? {
        configurationLoader.initLegacyData(tokenLoader::getDataNullable)
        val configurations = configurationLoader.getDataNullable()
        return configurations?.firstOrNull {
            isConfigurationEquals(configuration, it)
        }
    }

    override fun setConfiguration(configuration: UsedeskChatConfiguration) {
        var configurations = (configurationLoader.getDataNullable() ?: arrayOf()).map {
            when {
                isConfigurationEquals(configuration, it) -> configuration
                else -> it
            }
        }
        configurations = when (configuration) {
            !in configurations -> configurations + configuration
            else -> configurations
        }
        when {
            configurations.isEmpty() -> configurationLoader.clearData()
            else -> configurationLoader.setData(configurations.toTypedArray())
        }
    }

    private fun isConfigurationEquals(
        configurationA: UsedeskChatConfiguration,
        configurationB: UsedeskChatConfiguration
    ): Boolean = configurationA.clientEmail == configurationB.clientEmail &&
            configurationA.clientPhoneNumber == configurationB.clientPhoneNumber &&
            configurationA.clientName == configurationB.clientName
}