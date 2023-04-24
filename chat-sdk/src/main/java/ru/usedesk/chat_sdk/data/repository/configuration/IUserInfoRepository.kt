
package ru.usedesk.chat_sdk.data.repository.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal interface IUserInfoRepository {
    fun getConfiguration(): UsedeskChatConfiguration?

    fun updateConfiguration(onUpdate: UsedeskChatConfiguration.() -> UsedeskChatConfiguration)
}