package ru.usedesk.chat_sdk.internal.data.repository.api

import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.IFileInfoLoader
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.*
import ru.usedesk.chat_sdk.internal.domain.entity.OnMessageListener
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.io.IOException
import java.net.URL

@InjectConstructor
class ApiRepository(
        private val socketApi: SocketApi,
        private val httpApiLoader: IHttpApiLoader,
        private val fileInfoLoader: IFileInfoLoader
) : IApiRepository {

    private fun isConnected() = socketApi.isConnected

    @Throws(UsedeskException::class)
    override fun connect(url: String, actionListener: IUsedeskActionListener,
                         onMessageListener: OnMessageListener) {
        socketApi.connect(url, actionListener, onMessageListener)
    }

    @Throws(UsedeskException::class)
    override fun init(configuration: UsedeskChatConfiguration, token: String) {
        socketApi.sendRequest(InitChatRequest(token, configuration.companyId,
                configuration.url))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, feedback: UsedeskFeedback) {
        checkConnection()
        socketApi.sendRequest(SendFeedbackRequest(token, feedback))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, text: String) {
        checkConnection()
        socketApi.sendRequest(SendMessageRequest(token, RequestMessage(text)))
    }

    @Throws(UsedeskSocketException::class)
    private fun checkConnection() {
        if (!isConnected()) {
            throw UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED)
        }
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, usedeskFileInfo: UsedeskFileInfo) {
        checkConnection()
        val usedeskFile = fileInfoLoader.getFrom(usedeskFileInfo)
        socketApi.sendRequest(SendMessageRequest(token, RequestMessage(usedeskFile)))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, email: String, name: String?, phone: Long?, additionalId: Long?) {
        socketApi.sendRequest(SetEmailRequest(token, email, name, phone, additionalId))
    }

    @Throws(UsedeskException::class)
    override fun send(configuration: UsedeskChatConfiguration, offlineForm: UsedeskOfflineForm) {
        try {
            val url = URL(configuration.offlineFormUrl)
            val postUrl = String.format(OFFLINE_FORM_PATH, url.host)
            httpApiLoader.post(postUrl, offlineForm)
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        }
    }

    override fun disconnect() {
        socketApi.disconnect()
    }

    companion object {
        private const val OFFLINE_FORM_PATH = "https://%1s/widget.js/"
    }
}