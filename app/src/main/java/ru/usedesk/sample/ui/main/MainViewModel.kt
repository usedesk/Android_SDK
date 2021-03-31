package ru.usedesk.sample.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import ru.usedesk.chat_sdk.UsedeskChatSdk.setConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.setConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository

class MainViewModel : ViewModel() {
    private val configurationRepository: ConfigurationRepository = ServiceLocator.configurationRepository
    private val disposables = CompositeDisposable()
    private var inited = false

    val configurationLiveData = MutableLiveData<Configuration?>()
    val errorLiveData = MutableLiveData<UsedeskEvent<String>?>()

    private lateinit var mainNavigation: MainNavigation
    private lateinit var configuration: Configuration

    fun init(mainNavigation: MainNavigation) {
        this.mainNavigation = mainNavigation
        if (!inited) {
            inited = true
            mainNavigation.goConfiguration()
        }
    }

    fun goShowFile(usedeskFile: UsedeskFile) {
        mainNavigation.goShowFile(usedeskFile)
    }

    fun goSdk() {
        disposables.add(configurationRepository.getConfiguration().subscribe { configuration: Configuration ->
            val defaultChatConfiguration = UsedeskChatConfiguration(
                    urlChat = configuration.urlChat,
                    companyId = configuration.companyId,
                    channelId = configuration.channelId,
            )
            var urlToSendFile = configuration.urlToSendFile
            if (urlToSendFile.isEmpty()) {
                urlToSendFile = defaultChatConfiguration.urlToSendFile
            }
            var urlOfflineForm = configuration.urlOfflineForm
            if (urlOfflineForm.isEmpty()) {
                urlOfflineForm = defaultChatConfiguration.urlOfflineForm
            }
            val usedeskChatConfiguration = UsedeskChatConfiguration(
                    configuration.urlChat,
                    urlOfflineForm,
                    urlToSendFile,
                    configuration.companyId,
                    configuration.channelId,
                    configuration.clientSignature,
                    configuration.clientEmail,
                    configuration.clientName,
                    configuration.clientNote,
                    configuration.clientPhoneNumber,
                    configuration.clientAdditionalId,
                    configuration.clientInitMessage)
            if (usedeskChatConfiguration.validate().isAllValid()) {
                this.configuration = configuration
                initUsedeskConfiguration(usedeskChatConfiguration, configuration.withKb)
                configurationLiveData.postValue(configuration)
                if (this.configuration.withKb) {
                    mainNavigation.goKnowledgeBase(configuration.withKbSupportButton,
                            configuration.withKbArticleRating)
                } else {
                    mainNavigation.goChat(configuration.customAgentName)
                }
            } else {
                errorLiveData.postValue(UsedeskSingleLifeEvent("Invalid configuration"))
            }
        })
    }

    fun goChat() {
        disposables.add(configurationRepository.getConfiguration().subscribe { configuration: Configuration ->
            mainNavigation.goChat(configuration.customAgentName)
        })
    }

    fun onBackPressed() {
        mainNavigation.onBackPressed()
    }

    private fun initUsedeskConfiguration(usedeskChatConfiguration: UsedeskChatConfiguration,
                                         withKnowledgeBase: Boolean) {
        setConfiguration(usedeskChatConfiguration)
        if (withKnowledgeBase) {
            val defaultConfiguration = UsedeskKnowledgeBaseConfiguration(
                    configuration.accountId,
                    configuration.token,
                    configuration.clientEmail
            )
            var urlApi = configuration.urlApi
            if (urlApi.isEmpty()) {
                urlApi = defaultConfiguration.urlApi
            }
            setConfiguration(UsedeskKnowledgeBaseConfiguration(
                    urlApi,
                    configuration.accountId,
                    configuration.token,
                    configuration.clientEmail,
                    configuration.clientName))
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}