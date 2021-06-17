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
    override fun getConfiguration(
        configuration: UsedeskChatConfiguration
    ): UsedeskChatConfiguration {
        return getConfigurationNullable(configuration) ?: throw UsedeskDataNotFoundException()
    }

    override fun getConfigurationNullable(
        configuration: UsedeskChatConfiguration
    ): UsedeskChatConfiguration? {
        configurationDataLoader.initLegacyData {
            tokenDataLoader.getDataNullable()
        }
        val configurations = configurationDataLoader.getDataNullable()
        return configurations?.firstOrNull {
            isConfigurationEquals(configuration, it)
        }
    }

    override fun setConfiguration(configuration: UsedeskChatConfiguration) {
        var configurations = (configurationDataLoader.getDataNullable() ?: arrayOf()).map {
            if (isConfigurationEquals(configuration, it)) {
                configuration
            } else {
                it
            }
        }
        configurations = if (configuration !in configurations) {
            configurations + configuration
        } else {
            configurations
        }
        if (configurations.isEmpty()) {
            configurationDataLoader.clearData()
        } else {
            configurationDataLoader.setData(configurations.toTypedArray())
        }
    }

    private fun isConfigurationEquals(
        configurationA: UsedeskChatConfiguration,
        configurationB: UsedeskChatConfiguration
    ): Boolean {
        return configurationA.clientEmail == configurationB.clientEmail &&
                configurationA.clientPhoneNumber == configurationB.clientPhoneNumber &&
                configurationA.clientName == configurationB.clientName
    }
}