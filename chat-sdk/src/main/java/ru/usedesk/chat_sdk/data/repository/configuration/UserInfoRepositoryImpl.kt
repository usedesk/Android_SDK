package ru.usedesk.chat_sdk.data.repository.configuration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.ConfigurationsLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class UserInfoRepositoryImpl @Inject constructor(
    initConfiguration: UsedeskChatConfiguration,
    private val configurationLoader: ConfigurationsLoader
) : UserInfoRepository {

    private val _configurationFlow = MutableStateFlow(initConfiguration)
    override val configurationFlow = _configurationFlow.asStateFlow()

    override val clientTokenFlow: Flow<String?> = _configurationFlow.map { it.clientToken }
    override val clientTokenFlowNotNull: Flow<String> = clientTokenFlow.filterNotNull()

    init {
        // "" means that saved token should not be used
        when (initConfiguration.clientToken) {
            "" -> Unit
            null -> configurationLoader.getConfig().clientToken?.let(::setClientToken)
            else -> configurationLoader.setConfig(initConfiguration)
        }
    }

    override fun updateConfiguration(onUpdate: UsedeskChatConfiguration.() -> UsedeskChatConfiguration) {
        configurationLoader.setConfig(_configurationFlow.updateAndGet { it.onUpdate() })
    }

    override fun setClientToken(clientToken: String) {
        updateConfiguration { copy(clientToken = clientToken) }
    }

    override fun getConfiguration(): UsedeskChatConfiguration {
        return _configurationFlow.value
    }
}
