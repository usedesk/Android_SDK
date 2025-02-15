package ru.usedesk.chat_sdk.data.repository.configuration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal interface UserInfoRepository {
    val configurationFlow: StateFlow<UsedeskChatConfiguration>
    val clientTokenFlow: Flow<String?>
    val clientTokenFlowNotNull: Flow<String>

    fun updateConfiguration(onUpdate: UsedeskChatConfiguration.() -> UsedeskChatConfiguration)

    fun setClientToken(clientToken: String)

    fun getConfiguration(): UsedeskChatConfiguration
}