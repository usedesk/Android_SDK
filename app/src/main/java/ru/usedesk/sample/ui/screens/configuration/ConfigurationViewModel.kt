package ru.usedesk.sample.ui.screens.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_gui.UsedeskLiveData
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
    val avatarLiveData = MutableLiveData<String?>()

    val clientTokenLiveData = UsedeskLiveData(ClientToken())

    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        mainScope.launch {
            configurationRepository.getConfigurationFlow().collect {
                configurationLiveData.value = it
            }
        }
    }

    fun onGoSdkClick(configuration: Configuration): Boolean {
        val configurationValidation = validate(configuration)
        this.configurationValidation.postValue(configurationValidation)
        return if (configurationValidation.chatConfigurationValidation.isAllValid()
            && configurationValidation.knowledgeBaseConfiguration.isAllValid()
        ) {
            this.configurationLiveData.postValue(configuration)
            configurationRepository.setConfiguration(configuration)
            true
        } else {
            false
        }
    }

    fun onCreateChat(configuration: Configuration): Boolean {
        val configurationValidation = validate(configuration)
        this.configurationValidation.postValue(configurationValidation)
        return if (configurationValidation.chatConfigurationValidation.isAllValid()
            && configurationValidation.knowledgeBaseConfiguration.isAllValid()
        ) {
            this.configurationLiveData.postValue(configuration)
            configurationRepository.setConfiguration(configuration)
            true
        } else {
            false
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
            configuration.urlChatApi,
            configuration.companyId,
            configuration.channelId,
            configuration.messagesPageSize,
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

    fun createChat(apiToken: String) {
        clientTokenLiveData.value = clientTokenLiveData.value.copy(
            loading = true
        )
        val ignored = Single.create<String> {
            it.onSuccess(
                UsedeskChatSdk.requireInstance().createChat(apiToken)
            )
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe { clientToken, throwable ->
                clientTokenLiveData.value = clientTokenLiveData.value.copy(
                    loading = false,
                    completed = when {
                        clientToken != null -> UsedeskSingleLifeEvent(clientToken)
                        else -> null
                    },
                    error = when {
                        clientToken != null -> null
                        else -> UsedeskSingleLifeEvent(throwable?.message)
                    }
                )
                UsedeskChatSdk.release(false)
            }
    }

    data class ClientToken(
        val loading: Boolean = false,
        val completed: UsedeskSingleLifeEvent<String?>? = null,
        val error: UsedeskSingleLifeEvent<String?>? = null
    )
}