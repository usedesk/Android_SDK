package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException

interface IUserInfoRepository {

    @Throws(UsedeskDataNotFoundException::class)
    fun getConfiguration(configuration: UsedeskChatConfiguration): UsedeskChatConfiguration

    fun getConfigurationNullable(configuration: UsedeskChatConfiguration): UsedeskChatConfiguration?

    fun setConfiguration(configuration: UsedeskChatConfiguration)
}