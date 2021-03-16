package ru.usedesk.chat_sdk.data.repository.api

import android.net.Uri
import com.google.gson.Gson
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApi
import ru.usedesk.chat_sdk.data.repository.api.entity.FileResponse
import ru.usedesk.chat_sdk.data.repository.api.entity.OfflineFormRequest
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
        private val fileLoader: IFileLoader,
        apiFactory: IUsedeskApiFactory,
        gson: Gson
) : UsedeskApiRepository<IHttpApi>(apiFactory, gson, IHttpApi::class.java), IApiRepository {

    private fun isConnected() = socketApi.isConnected()

    private lateinit var eventListener: IApiRepository.EventListener

    private var callbackSettings = UsedeskOfflineFormSettings(false, UsedeskOfflineFormSettings.WorkType.NEVER)

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
            chatInited.callbackSettings.run {
                callbackSettings = this
                if ((workType == UsedeskOfflineFormSettings.WorkType.CHECK_WORKING_TIMES && noOperators)
                        || workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT
                        || workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT) {
                    if (workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT) {
                        eventListener.onChatInited(chatInited)
                    }
                    eventListener.onOfflineForm(this)
                } else {
                    eventListener.onChatInited(chatInited)
                }
            }
        }

        override fun onNew(messageResponse: MessageResponse) {
            val messages = messageResponseConverter.convert(messageResponse.message)
            messages.forEach {
                if (it is UsedeskMessageClient && it.id != it.localId) {
                    eventListener.onMessageUpdated(it)
                } else {
                    eventListener.onMessagesReceived(listOf(it))
                }
            }
        }

        override fun onSetEmailSuccess() {
            eventListener.onSetEmailSuccess()
        }
    }

    override fun connect(url: String,
                         token: String?,
                         configuration: UsedeskChatConfiguration,
                         eventListener: IApiRepository.EventListener) {
        this.eventListener = eventListener
        socketApi.connect(url, token, configuration.companyId, socketEventListener)
    }

    override fun init(configuration: UsedeskChatConfiguration,
                      token: String?) {
        socketApi.sendRequest(InitChatRequest(token,
                configuration.companyId,
                configuration.urlChat))
    }

    override fun send(token: String,
                      messageId: Long,
                      feedback: UsedeskFeedback) {
        checkConnection()
        socketApi.sendRequest(FeedbackRequest(token, messageId, feedback))
    }

    override fun send(token: String,
                      messageText: UsedeskMessageText) {
        checkConnection()
        val request = MessageRequest(token, messageText.text, messageText.id)
        socketApi.sendRequest(request)
    }

    override fun send(configuration: UsedeskChatConfiguration,
                      token: String,
                      messageFile: UsedeskMessageFile) {
        checkConnection()

        val loadedFile = fileLoader.load(Uri.parse(messageFile.file.content))
        val parts = listOf(
                multipartConverter.convert("chat_token", token),
                multipartConverter.convert("file", loadedFile),
                multipartConverter.convert("message_id", messageFile.id)
        )
        doRequest(configuration.urlToSendFile, FileResponse::class.java) {
            it.postFile(parts)
        }
    }

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