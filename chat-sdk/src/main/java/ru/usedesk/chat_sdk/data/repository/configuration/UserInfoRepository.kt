package ru.usedesk.chat_sdk.data.repository.configuration

import kotlinx.coroutines.flow.Flow
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal interface UserInfoRepository {
    val clientTokenFlow: Flow<String?>
    val clientTokenFlowNotNull: Flow<String>

    fun updateConfiguration(onUpdate: UsedeskChatConfiguration.() -> UsedeskChatConfiguration)

    fun setClientToken(clientToken: String)
}