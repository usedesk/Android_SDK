package ru.usedesk.chat_sdk.internal.data.repository.configuration

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException

interface IUserInfoRepository {
    @Throws(UsedeskDataNotFoundException::class)
    fun getToken(): String

    fun setToken(token: String?)

    @Throws(UsedeskDataNotFoundException::class)
    fun getConfiguration(): UsedeskChatConfiguration

    fun setConfiguration(configuration: UsedeskChatConfiguration?)
}