package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal interface ConfigurationsLoader {
    fun initLegacyData(onGetClientToken: () -> String?)

    fun getConfig(userKey: String): UsedeskChatConfiguration?

    fun setConfig(userKey: String, configuration: UsedeskChatConfiguration?)

    fun clearData()
}