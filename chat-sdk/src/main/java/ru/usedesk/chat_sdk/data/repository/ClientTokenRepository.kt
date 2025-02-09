package ru.usedesk.chat_sdk.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.di.chat.ChatScope
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

@ChatScope
internal class ClientTokenRepository @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    private val userInfoRepository: UserInfoRepository,
) {
    private val _clientTokenRepositoryFlow: MutableStateFlow<String?>
    val clientTokenRepositoryFlow: StateFlow<String?> get() = _clientTokenRepositoryFlow.asStateFlow()

    init {
        val oldConfiguration = userInfoRepository.getConfiguration()
        _clientTokenRepositoryFlow = MutableStateFlow(
            initConfiguration.clientToken
                ?: oldConfiguration?.clientToken
        )
    }

    fun setClientToken(clientToken: String) {
        _clientTokenRepositoryFlow.value = clientToken
        userInfoRepository.updateConfiguration { copy(clientToken = clientToken) }
    }
}