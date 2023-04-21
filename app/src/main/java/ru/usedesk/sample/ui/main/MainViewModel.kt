
package ru.usedesk.sample.ui.main

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.sample.ServiceLocator
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.ui.main.MainViewModel.Model

class MainViewModel : UsedeskViewModel<Model>(Model()) {
    private val configurationRepository = ServiceLocator.instance.configurationRepository

    private var downloadFile: DownloadFile? = null

    init {
        configurationRepository.configurationFlow.launchCollect {
            setModel { copy(configuration = it) }
        }
    }

    fun goSdk(configuration: Configuration) {
        setModel {
            val usedeskChatConfiguration = configuration.toChatConfiguration()
            when {
                usedeskChatConfiguration.validate().isAllValid() -> copy(
                    configuration = configuration,
                    goSdk = UsedeskEvent(configuration.kb.withKb)
                )
                else -> copy(
                    configuration = configuration,
                    error = UsedeskEvent("Invalid configuration")
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
        val configuration = configurationRepository.configurationFlow.value
        val newConfiguration = configuration
            .copy(chat = configuration.chat.copy(clientToken = clientToken))

        configurationRepository.setConfiguration(newConfiguration)
    }

    data class Model(
        val configuration: Configuration = Configuration(),
        val error: UsedeskEvent<String>? = null,
        val goSdk: UsedeskEvent<Any>? = null
    )

    data class DownloadFile(
        val url: String,
        val name: String
    )
}