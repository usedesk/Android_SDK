package ru.usedesk.sample.ui.screens.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation

class ConfigurationViewModel : ViewModel() {
    private val configurationRepository = ServiceLocator.configurationRepository

    val configurationLiveData = UsedeskLiveData(
        configurationRepository.getConfigurationFlow().value
    )
    val configurationValidation = MutableLiveData<ConfigurationValidation?>()
    val goToSdkEvent = MutableLiveData<UsedeskEvent<Any?>?>()
    val avatarLiveData = MutableLiveData<String?>()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        mainScope.launch {
            configurationRepository.getConfigurationFlow().collect {
                configurationLiveData.value = it
            }
        }
    }

    fun onGoSdkClick(configuration: Configuration) {
        val configurationValidation = validate(configuration)
        this.configurationValidation.postValue(configurationValidation)
        if (configurationValidation.chatConfigurationValidation.isAllValid()
            && configurationValidation.knowledgeBaseConfiguration.isAllValid()
        ) {
            this.configurationLiveData.postValue(configuration)
            configurationRepository.setConfiguration(configuration)
            goToSdkEvent.postValue(UsedeskSingleLifeEvent(null))
        }
    }

    fun setTempConfiguration(configuration: Configuration) {
        this.configurationLiveData.value = configuration
    }

    fun setAvatar(avatar: String?) {
        avatarLiveData.value = avatar
    }

    private fun validate(configuration: Configuration): ConfigurationValidation {
        val chatValidation = UsedeskChatConfiguration(
            configuration.urlChat,
            configuration.urlOfflineForm,
            configuration.companyId,
            configuration.channelId,
            configuration.clientToken,
            configuration.clientEmail,
            configuration.clientName,
            configuration.clientNote,
            configuration.clientPhoneNumber,
            configuration.clientAdditionalId,
            configuration.clientInitMessage,
            null,
            configuration.cacheFiles
        ).validate()
        val knowledgeBaseValidation = UsedeskKnowledgeBaseConfiguration(
            configuration.urlApi,
            configuration.accountId,
            configuration.token,
            configuration.clientEmail,
            configuration.clientName
        ).validate()
        return ConfigurationValidation(
            chatValidation,
            knowledgeBaseValidation
        )
    }

    override fun onCleared() {
        super.onCleared()

        mainScope.cancel()
    }

    fun isMaterialComponentsSwitched(configuration: Configuration): Boolean {
        return if (configuration.materialComponents != configurationLiveData.value.materialComponents) {
            configurationRepository.setConfiguration(configuration)
            true
        } else {
            false
        }
    }
}