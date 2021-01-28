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
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail.SetEmailRequest
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.io.IOException

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

    private var localId = 0L

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
                eventListener.onMessagesReceived(messages)
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
    override fun init(configuration: UsedeskChatConfiguration, token: String?) {
        socketApi.sendRequest(InitChatRequest(token, configuration.companyId,
                configuration.urlChat))
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
        localId--
        /*val calendar = Calendar.getInstance()
        val file = UsedeskFile.create(
                usedeskFileInfo.uri.toString(),
                usedeskFileInfo.type,
                "",
                usedeskFileInfo.name
        )
        val tempMessage = if (usedeskFileInfo.isImage()) {
            UsedeskMessageClientImage(localId, calendar, file, UsedeskMessageClient.Status.SENDING)
        } else {
            UsedeskMessageClientFile(localId, calendar, file, UsedeskMessageClient.Status.SENDING)
        }
        eventListener.onMessagesReceived(listOf(tempMessage))*/

        val loadedFile = fileLoader.load(usedeskFileInfo.uri)
        val parts = listOf(
                multipartConverter.convert("chat_token", token),
                multipartConverter.convert("file", loadedFile)
        )
        val fileResponse = doRequest(configuration.urlToSendFile, FileResponse::class.java) {
            it.postFile(parts)
        }.apply {
            id = localId.toString()
            type = loadedFile.type
            name = loadedFile.name
        }
        val message = fileResponseConverter.convert(fileResponse)
        //eventListener.onMessageUpdated(message)//TODO: временно, пока сервер не позволит определять какой файл ушёл
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
            doRequest(configuration.urlOfflineForm, Array<Any>::class.java) {
                val request = OfflineFormRequest(companyId,
                        offlineForm.name,
                        offlineForm.email,
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