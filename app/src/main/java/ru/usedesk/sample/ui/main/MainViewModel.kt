package ru.usedesk.sample.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.setConfiguration
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration

class MainViewModel : ViewModel() {
    private val configurationRepository = ServiceLocator.configurationRepository

    val configurationLiveData: UsedeskLiveData<Configuration> = UsedeskLiveData(
        configurationRepository.getConfigurationFlow().value
    )
    val errorLiveData = MutableLiveData<UsedeskEvent<String>?>()
    val goSdkEventLiveData = MutableLiveData<UsedeskEvent<Any>?>()

    private var downloadFile: DownloadFile? = null

    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        mainScope.launch {
            configurationRepository.getConfigurationFlow().collect {
                configurationLiveData.value = it
            }
        }
    }

    fun goSdk() {
        val configuration = configurationRepository.getConfigurationFlow().value

        val usedeskChatConfiguration = configuration.toChatConfiguration()
        if (usedeskChatConfiguration.validate().isAllValid()) {
            initUsedeskConfiguration(configuration.withKb)
            configurationLiveData.postValue(configuration)
            if (configuration.withKb) {
                goSdkEventLiveData.postValue(UsedeskSingleLifeEvent(true))
            } else {
                goSdkEventLiveData.postValue(UsedeskSingleLifeEvent(false))
            }
        } else {
            errorLiveData.postValue(UsedeskSingleLifeEvent("Invalid configuration"))
        }
    }

    private fun initUsedeskConfiguration(withKnowledgeBase: Boolean) {
        if (withKnowledgeBase) {
            val configuration = configurationLiveData.value
            val kbConfiguration = configuration.toKbConfiguration()
            setConfiguration(kbConfiguration)
        }
    }

    fun setDownloadFile(downloadFile: DownloadFile) {
        this.downloadFile = downloadFile
    }

    fun useDownloadFile(onDownloadFile: (DownloadFile) -> Unit) {
        downloadFile?.let {
            downloadFile = null
            onDownloadFile(it)
        }
    }

    fun onClientToken(clientToken: String) {
        val newConfiguration = configurationRepository.getConfigurationFlow().value
            .copy(clientToken = clientToken)

        configurationRepository.setConfiguration(newConfiguration)
    }

    override fun onCleared() {
        super.onCleared()
        mainScope.cancel()
    }

    data class DownloadFile(
        val url: String,
        val name: String
    )
}