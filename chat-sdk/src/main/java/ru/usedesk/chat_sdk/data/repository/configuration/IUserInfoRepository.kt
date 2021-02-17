package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException

interface IUserInfoRepository {
    @Throws(UsedeskDataNotFoundException::class)
    fun getToken(): String

    fun setToken(token: String?)

    @Throws(UsedeskDataNotFoundException::class)
    fun getConfiguration(): UsedeskChatConfiguration

    fun getConfigurationNullable(): UsedeskChatConfiguration?

    fun setConfiguration(configuration: UsedeskChatConfiguration?)
}