package ru.usedesk.sample.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.setConfiguration
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration

class MainViewModel : ViewModel() {
    private val configurationRepository = ServiceLocator.configurationRepository
    private val disposables = mutableListOf<Disposable>()

    val configurationLiveData = UsedeskLiveData(Configuration())
    val errorLiveData = MutableLiveData<UsedeskEvent<String>?>()
    val goSdkEventLiveData = MutableLiveData<UsedeskEvent<Any>?>()

    private var downloadFile: DownloadFile? = null

    init {
        disposables.add(configurationRepository.getConfigurationObservable().subscribe {
            configurationLiveData.postValue(it)
        })
    }

    fun goSdk() {
        disposables.add(
            configurationRepository.getConfiguration().subscribe { configuration: Configuration ->
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
            })
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

    override fun onCleared() {
        super.onCleared()
        disposables.forEach {
            it.dispose()
        }
    }

    fun onClientToken(clientToken: String) {
        val ignore = configurationRepository.getConfiguration().map {
            it.copy(clientToken = clientToken)
        }.subscribe { newConfiguration ->
            configurationRepository.setConfiguration(newConfiguration).subscribe()
        }
    }

    data class DownloadFile(
        val url: String,
        val name: String
    )
}