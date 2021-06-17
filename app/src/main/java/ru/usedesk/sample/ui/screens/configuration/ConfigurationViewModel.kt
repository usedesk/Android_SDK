package ru.usedesk.sample.ui.screens.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation

class ConfigurationViewModel : ViewModel() {
    private val configurationRepository = ServiceLocator.configurationRepository
    private val disposables = CompositeDisposable()

    val configurationLiveData = MutableLiveData<Configuration?>()
    val configurationValidation = MutableLiveData<ConfigurationValidation?>()
    val goToSdkEvent = MutableLiveData<UsedeskEvent<Any?>?>()

    init {
        disposables.add(configurationRepository.getConfigurationObservable().subscribe {
            configurationLiveData.postValue(it)
        })
        configurationRepository.getConfiguration().subscribe()
    }

    fun onGoSdkClick(configuration: Configuration) {
        val configurationValidation = validate(configuration)
        this.configurationValidation.postValue(configurationValidation)
        if (configurationValidation.chatConfigurationValidation.isAllValid()
                && configurationValidation.knowledgeBaseConfiguration.isAllValid()) {
            this.configurationLiveData.postValue(configuration)
            configurationRepository.setConfiguration(configuration)
                    .subscribe()
            goToSdkEvent.postValue(UsedeskSingleLifeEvent(null))
        }
    }

    fun setTempConfiguration(configuration: Configuration) {
        this.configurationLiveData.value = configuration
    }

    private fun validate(configuration: Configuration): ConfigurationValidation {
        val chatValidation = UsedeskChatConfiguration(
            configuration.urlChat,
            configuration.urlOfflineForm,
            configuration.urlToSendFile,
            configuration.companyId,
            configuration.channelId,
            configuration.clientToken,
            configuration.clientEmail,
            configuration.clientName,
            configuration.clientNote,
            configuration.clientPhoneNumber,
            configuration.clientAdditionalId,
            configuration.clientInitMessage
        ).validate()
        val knowledgeBaseValidation = UsedeskKnowledgeBaseConfiguration(
                configuration.urlApi,
                configuration.accountId,
                configuration.token,
                configuration.clientEmail,
                configuration.clientName
        ).validate()
        return ConfigurationValidation(chatValidation,
                knowledgeBaseValidation,
                configuration.withKb)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}