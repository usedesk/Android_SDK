package ru.usedesk.chat_sdk.data.repository.configuration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val configurationFlow = MutableStateFlow(initConfiguration)

    override val clientTokenFlow: Flow<String?> = configurationFlow.map { it.clientToken }
    override val clientTokenFlowNotNull: Flow<String> = clientTokenFlow.filterNotNull()

    init {
        configurationLoader.getConfig(initConfiguration.userKey())?.let { configuration ->
            // "" means that saved token should not be used
            if (initConfiguration.clientToken != "") {
                updateConfiguration { configuration }
            }
        }
    }

    override fun updateConfiguration(onUpdate: UsedeskChatConfiguration.() -> UsedeskChatConfiguration) {
        val configuration = configurationFlow.updateAndGet { it.onUpdate() }
        configurationLoader.setConfig(configuration.userKey(), configuration)
    }

    override fun setClientToken(clientToken: String) {
        updateConfiguration { copy(clientToken = clientToken) }
    }
}
