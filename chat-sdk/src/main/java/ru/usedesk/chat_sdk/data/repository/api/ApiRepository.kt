package ru.usedesk.chat_sdk.data.repository.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApi
import ru.usedesk.chat_sdk.data.repository.api.entity.AdditionalFieldsRequest
import ru.usedesk.chat_sdk.data.repository.api.entity.FileResponse
import ru.usedesk.chat_sdk.data.repository.api.entity.SetClientResponse
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
import java.io.File
import java.io.IOException
import java.util.*

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
            chatInited.offlineFormSettings.run {
                if ((workType == UsedeskOfflineFormSettings.WorkType.CHECK_WORKING_TIMES
                            && noOperators)
                    || (workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT
                            && initChatResponse.setup?.ticket?.statusId in STATUSES_FOR_FORM)
                    || workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT
                ) {
                    eventListener.onOfflineForm(this, chatInited)
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

    override fun connect(
        url: String,
        token: String?,
        configuration: UsedeskChatConfiguration,
        eventListener: IApiRepository.EventListener
    ) {
        this.eventListener = eventListener
        socketApi.connect(url, token, configuration.getCompanyAndChannel(), socketEventListener)
    }

    override fun init(
        configuration: UsedeskChatConfiguration,
        token: String?
    ) {
        socketApi.sendRequest(
            InitChatRequest(
                token,
                configuration.companyId,
                configuration.urlChat
            )
        )
    }

    override fun send(
        messageId: Long,
        feedback: UsedeskFeedback
    ) {
        checkConnection()
        socketApi.sendRequest(FeedbackRequest(messageId, feedback))
    }

    override fun send(messageText: UsedeskMessageText) {
        checkConnection()
        val request = MessageRequest(messageText.text, messageText.id)
        socketApi.sendRequest(request)
    }

    override fun send(
        configuration: UsedeskChatConfiguration,
        token: String,
        fileInfo: UsedeskFileInfo,
        messageId: Long
    ) {
        checkConnection()

        val file = File(fileInfo.uri.path)
        val fileRequestBody = RequestBody.create(MediaType.parse(fileInfo.type), file)
        val parts = listOf(
            multipartConverter.convert("chat_token", token),
            MultipartBody.Part.createFormData("file", fileInfo.name, fileRequestBody),
            multipartConverter.convert("message_id", messageId)
        )
        doRequest(configuration.urlToSendFile, FileResponse::class.java) {
            it.postFile(parts)
        }
    }

    override fun send(
        token: String,
        email: String?,
        name: String?,
        note: String?,
        phone: Long?,
        additionalId: Long?
    ) {
        socketApi.sendRequest(
            SetClientRequest(
                token,
                email,
                name,
                note,
                phone,
                additionalId
            )
        )
    }

    override fun send(
        configuration: UsedeskChatConfiguration,
        companyId: String,
        offlineForm: UsedeskOfflineForm
    ) {
        try {
            doRequest(configuration.urlOfflineForm, Array<Any>::class.java) {
                val params = mapOf(
                    "email" to getCorrectStringValue(offlineForm.clientEmail),
                    "name" to getCorrectStringValue(offlineForm.clientName),
                    "company_id" to getCorrectStringValue(companyId),
                    "message" to getCorrectStringValue(offlineForm.message),
                    "topic" to getCorrectStringValue(offlineForm.topic)
                )
                val customFields = offlineForm.fields.filter { field ->
                    field.value.isNotEmpty()
                }.map { field ->
                    field.key to getCorrectStringValue(field.value)
                }
                val json = JsonObject()
                (params + customFields).forEach { param ->
                    json.addProperty(param.key, param.value)
                }
                it.sendOfflineForm(json)
            }
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        }
    }

    override fun send(
        token: String?,
        configuration: UsedeskChatConfiguration,
        additionalFields: Map<Long, String>,
        additionalNestedFields: List<Map<Long, String>>
    ) {
        val response = doRequest(configuration.urlToSendFile, SetClientResponse::class.java) {
            if (token != null) {
                val totalFields =
                    (additionalFields.toList() + additionalNestedFields.flatMap { fields ->
                        fields.toList()
                    }).map { field ->
                        AdditionalFieldsRequest.AdditionalField(field.first, field.second)
                    }
                val request = AdditionalFieldsRequest(token, totalFields)
                it.postAdditionalFields(request)
            } else {
                throw UsedeskHttpException("Token is null")
            }
        }
    }

    private fun getCorrectStringValue(value: String) = value.replace("\"", "\\\"")

    override fun disconnect() {
        socketApi.disconnect()
    }

    private fun checkConnection() {
        if (!isConnected()) {
            throw UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED)
        }
    }

    companion object {
        private val STATUSES_FOR_FORM = listOf(null, 2, 3, 4, 7, 9, 10)
    }
}