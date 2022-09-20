package ru.usedesk.sample.ui.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.ui.main.MainViewModel.Model

class MainViewModel : UsedeskViewModel<Model>(Model()) {
    private val configurationRepository = ServiceLocator.configurationRepository

    private var downloadFile: DownloadFile? = null

    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        mainScope.launch {
            configurationRepository.getConfigurationFlow().collect {
                setModel { copy(configuration = it) }
            }
        }
    }

    fun first() {
        setModel { copy(first = true) }
    }

    fun second() {
        setModel { copy(second = true) }
    }

    fun goSdk() {
        setModel {
            val usedeskChatConfiguration = configuration.toChatConfiguration()
            when {
                usedeskChatConfiguration.validate().isAllValid() -> copy(
                    goSdk = UsedeskSingleLifeEvent(configuration.withKb)
                )
                else -> copy(
                    error = UsedeskSingleLifeEvent("Invalid configuration")
                )
            }
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

    data class Model(
        val configuration: Configuration = Configuration(),
        val error: UsedeskEvent<String>? = null,
        val goSdk: UsedeskEvent<Any>? = null,
        val first: Boolean = false,
        val second: Boolean = false
    )

    data class DownloadFile(
        val url: String,
        val name: String
    )
}