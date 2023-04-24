
package ru.usedesk.sample.ui.screens.configuration

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskPreparation
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation
import ru.usedesk.sample.ui.screens.configuration.ConfigurationViewModel.Model

class ConfigurationViewModel : UsedeskViewModel<Model>(Model()) {
    private val configurationRepository = ServiceLocator.instance.configurationRepository

    data class Model(
        val configuration: Configuration = Configuration(),
        val validation: ConfigurationValidation? = null,
        val avatar: String? = null,
        val clientToken: ClientToken = ClientToken()
    )

    init {
        configurationRepository.configurationFlow.launchCollect {
            setModel { copy(configuration = it) }
        }
    }

    fun onGoSdkClick(configuration: Configuration): Boolean {
        val configurationValidation = validate(configuration)
        setModel { copy(validation = configurationValidation) }
        return if (configurationValidation.chatConfigurationValidation.isAllValid()
            && configurationValidation.knowledgeBaseConfiguration.isAllValid()
        ) {
            configurationRepository.setConfiguration(configuration)
            true
        } else {
            false
        }
    }

    fun onCreateChat(configuration: Configuration): Boolean {
        val configurationValidation = validate(configuration)
        setModel { copy(validation = configurationValidation) }
        return if (configurationValidation.chatConfigurationValidation.isAllValid()
            && configurationValidation.knowledgeBaseConfiguration.isAllValid()
        ) {
            configurationRepository.setConfiguration(configuration)
            true
        } else {
            false
        }
    }

    fun setTempConfiguration(configuration: Configuration) {
        setModel { copy(configuration = configuration) }
    }

    fun setAvatar(avatar: String?) {
        setModel { copy(avatar = avatar) }
    }

    private fun validate(configuration: Configuration): ConfigurationValidation {
        val chatValidation = configuration.toChatConfiguration().validate()
        val knowledgeBaseValidation = configuration.toKbConfiguration().validate()
        return ConfigurationValidation(
            chatValidation,
            knowledgeBaseValidation
        )
    }

    fun isMaterialComponentsSwitched(configuration: Configuration): Boolean =
        when (configuration.common.materialComponents) {
            modelFlow.value.configuration.common.materialComponents -> false
            else -> {
                configurationRepository.setConfiguration(configuration)
                true
            }
        }

    fun createChat(
        preparationInteractor: IUsedeskPreparation,
        apiToken: String
    ) {
        setModel {
            when {
                clientToken.loading -> this
                else -> {
                    preparationInteractor.createChat(apiToken) { result ->
                        setModel {
                            copy(
                                clientToken = when (result) {
                                    is IUsedeskPreparation.CreateChatResult.Done -> clientToken.copy(
                                        loading = false,
                                        completed = UsedeskEvent(result.clientToken)
                                    )
                                    IUsedeskPreparation.CreateChatResult.Error -> clientToken.copy(
                                        loading = false,
                                        error = UsedeskEvent(Unit)
                                    )
                                }
                            )
                        }

                        UsedeskChatSdk.releasePreparation()
                    }
                    copy(clientToken = clientToken.copy(loading = true))
                }
            }
        }
    }

    data class ClientToken(
        val loading: Boolean = false,
        val completed: UsedeskEvent<String?>? = null,
        val error: UsedeskEvent<Unit>? = null
    )
}