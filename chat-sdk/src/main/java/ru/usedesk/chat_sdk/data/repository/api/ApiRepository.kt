package ru.usedesk.chat_sdk.data.repository.api

import ru.usedesk.chat_sdk.data.repository._extra.multipart.IMultipartConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.ChatInitedConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.ChatItemConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.IFileApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.IOfflineFormApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.entity.OfflineFormRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket.SocketApi
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.feedback.FeedbackRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail.SetEmailRequest
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.io.IOException
import java.net.URL

@InjectConstructor
internal class ApiRepository(
        private val socketApi: SocketApi,
        private val offlineFormApi: IOfflineFormApi,
        private val fileApi: IFileApi,
        private val multipartConverter: IMultipartConverter,
        private val chatInitedConverter: ChatInitedConverter,
        private val chatItemConverter: ChatItemConverter
) : IApiRepository {

    private fun isConnected() = socketApi.isConnected()

    @Throws(UsedeskException::class)
    override fun connect(url: String,
                         token: String?,
                         configuration: UsedeskChatConfiguration,
                         eventListener: IApiRepository.EventListener) {
        socketApi.connect(url, token, configuration.companyId, getEventListener(eventListener))
    }

    @Throws(UsedeskException::class)
    override fun init(configuration: UsedeskChatConfiguration, token: String?) {
        socketApi.sendRequest(InitChatRequest(token, configuration.companyId,
                configuration.url))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, feedback: UsedeskFeedback) {
        checkConnection()
        socketApi.sendRequest(FeedbackRequest(token, feedback))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, text: String) {
        checkConnection()
        socketApi.sendRequest(MessageRequest(token, text))
    }

    @Throws(UsedeskException::class)
    override fun send(configuration: UsedeskChatConfiguration,
                      token: String,
                      usedeskFileInfo: UsedeskFileInfo) {
        checkConnection()
        val url = configuration.url
        val parts = listOf(
                multipartConverter.makePart("chat_token", token),
                multipartConverter.makePart("file", usedeskFileInfo.uri)
        )
        fileApi.post(url, parts)
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, email: String, name: String?, phone: Long?, additionalId: Long?) {
        socketApi.sendRequest(SetEmailRequest(token, email, name, phone, additionalId))
    }

    @Throws(UsedeskException::class)
    override fun send(configuration: UsedeskChatConfiguration,
                      companyId: String,
                      offlineForm: UsedeskOfflineForm) {
        try {
            val url = URL(configuration.offlineFormUrl)
            val postUrl = String.format(OFFLINE_FORM_PATH, url.host)
            offlineFormApi.post(postUrl, OfflineFormRequest(companyId, offlineForm))
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        }
    }

    override fun disconnect() {
        socketApi.disconnect()
    }

    private fun checkConnection() {
        if (!isConnected()) {
            throw UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED)
        }
    }

    private fun getEventListener(eventListener: IApiRepository.EventListener): SocketApi.EventListener {
        return object : SocketApi.EventListener {
            override fun onConnected() {
                eventListener.onConnected()
            }

            override fun onDisconnected() {
                eventListener.onDisconnected()
            }

            override fun onTokenError() {
                eventListener.onTokenError()
            }

            override fun onFeedback() {
                eventListener.onFeedback()
            }

            override fun onException(exception: Exception) {
                eventListener.onException(exception)
            }

            override fun onInited(initChatResponse: InitChatResponse) {
                val chatInited = chatInitedConverter.convert(initChatResponse)
                if (chatInited.noOperators) {
                    eventListener.onOfflineForm()
                } else {
                    eventListener.onChatInited(chatInited)
                }
            }

            override fun onNew(messageResponse: MessageResponse) {
                if (messageResponse.message?.payload?.noOperators == true) {
                    eventListener.onOfflineForm()
                } else {
                    val chatItems = chatItemConverter.convert(messageResponse.message)
                    eventListener.onNewChatItems(chatItems)
                }
            }
        }
    }

    companion object {
        private const val OFFLINE_FORM_PATH = "https://%1s/widget.js/"
    }
}