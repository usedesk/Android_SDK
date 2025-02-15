package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal interface ConfigurationsLoader {
    fun initLegacyData(onGetClientToken: () -> String?)

    fun getConfig(): UsedeskChatConfiguration

    fun setConfig(configuration: UsedeskChatConfiguration?)

    fun clearData()
}