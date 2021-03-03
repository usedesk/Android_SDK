package ru.usedesk.chat_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApi
import ru.usedesk.chat_sdk.data.repository.api.entity.FileResponse
import ru.usedesk.chat_sdk.data.repository.api.entity.OfflineFormRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.FileResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.InitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.data.repository.api.loader.multipart.IMultipartConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket.SocketApi
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.feedback.FeedbackRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail.SetClientRequest
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.io.IOException
import java.util.*

@InjectConstructor
internal class ApiRepository(
        private val socketApi: SocketApi,
        private val multipartConverter: IMultipartConverter,
        private val initChatResponseConverter: InitChatResponseConverter,
        private val messageResponseConverter: MessageResponseConverter,
        private val fileResponseConverter: FileResponseConverter,
        private val fileLoader: IFileLoader,
        apiFactory: IUsedeskApiFactory,
        gson: Gson
) : UsedeskApiRepository<IHttpApi>(apiFactory, gson, IHttpApi::class.java), IApiRepository {

    private var localId = -1000L

    private fun isConnected() = socketApi.isConnected()

    private lateinit var eventListener: IApiRepository.EventListener

    private val socketEventListener = object : SocketApi.EventListener {
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
            val chatInited = initChatResponseConverter.convert(initChatResponse)
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
                val messages = messageResponseConverter.convert(messageResponse.message)
                messages.forEach {
                    if (it.id < 0) {
                        eventListener.onMessageUpdated(it)
                    } else {
                        eventListener.onMessagesReceived(listOf(it))
                    }
                }
            }
        }

        override fun onSetEmailSuccess() {
            eventListener.onSetEmailSuccess()
        }
    }

    @Throws(UsedeskException::class)
    override fun connect(url: String,
                         token: String?,
                         configuration: UsedeskChatConfiguration,
                         eventListener: IApiRepository.EventListener) {
        this.eventListener = eventListener
        socketApi.connect(url, token, configuration.companyId, socketEventListener)
    }

    @Throws(UsedeskException::class)
    override fun init(configuration: UsedeskChatConfiguration,
                      token: String?) {
        socketApi.sendRequest(InitChatRequest(token,
                configuration.companyId,
                configuration.urlChat))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String,
                      messageId: Long,
                      feedback: UsedeskFeedback) {
        checkConnection()
        socketApi.sendRequest(FeedbackRequest(token, messageId, feedback))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String,
                      text: String) {
        val sendingMessage = createSendingMessage(text)
        eventListener.onMessagesReceived(listOf(sendingMessage))

        checkConnection()
        socketApi.sendRequest(MessageRequest(token, text, sendingMessage.id))
    }

    @Throws(UsedeskException::class)
    override fun send(configuration: UsedeskChatConfiguration,
                      token: String,
                      fileInfo: UsedeskFileInfo) {
        val sendingMessage = createSendingMessage(fileInfo)
        eventListener.onMessagesReceived(listOf(sendingMessage))

        checkConnection()

        val loadedFile = fileLoader.load(fileInfo.uri)
        val parts = listOf(
                multipartConverter.convert("chat_token", token),
                multipartConverter.convert("file", loadedFile),
                multipartConverter.convert("message_id", sendingMessage.id)
        )
        doRequest(configuration.urlToSendFile, FileResponse::class.java) {
            it.postFile(parts)
        }
    }

    private fun createSendingMessage(text: String): UsedeskMessage {
        localId--
        val calendar = Calendar.getInstance()
        return UsedeskMessageClientText(localId, calendar, text, UsedeskMessageClient.Status.SENDING)
    }

    private fun createSendingMessage(fileInfo: UsedeskFileInfo): UsedeskMessage {
        localId--
        val calendar = Calendar.getInstance()
        val file = UsedeskFile.create(
                fileInfo.uri.toString(),
                fileInfo.type,
                "",
                fileInfo.name
        )
        return if (fileInfo.isImage()) {
            UsedeskMessageClientImage(localId, calendar, file, UsedeskMessageClient.Status.SENDING)
        } else {
            UsedeskMessageClientFile(localId, calendar, file, UsedeskMessageClient.Status.SENDING)
        }
    }

    @Throws(UsedeskException::class)
    override fun send(token: String?,
                      signature: String?,
                      email: String?,
                      name: String?,
                      note: String?,
                      phone: Long?,
                      additionalId: Long?) {
        socketApi.sendRequest(SetClientRequest(
                if (signature?.isNotEmpty() == true) {
                    null
                } else {
                    token
                }, signature, email, name, note, phone, additionalId))
    }

    @Throws(UsedeskException::class)
    override fun send(configuration: UsedeskChatConfiguration,
                      companyId: String,
                      offlineForm: UsedeskOfflineForm) {
        try {
            doRequest(configuration.urlOfflineForm, Array<Any>::class.java) {
                val request = OfflineFormRequest(companyId,
                        offlineForm.clientName,
                        offlineForm.clientEmail,
                        offlineForm.message)
                it.sendOfflineForm(request)
            }
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
}