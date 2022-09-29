package ru.usedesk.chat_sdk.data.repository.api

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.RetrofitApi
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.EventListener
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SendResult
import ru.usedesk.chat_sdk.data.repository.api.entity.*
import ru.usedesk.chat_sdk.data.repository.api.loader.InitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket.SocketApi
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.feedback.FeedbackRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter.FileBytes
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.math.min

internal class ApiRepository @Inject constructor(
    private val socketApi: SocketApi,
    private val initChatResponseConverter: InitChatResponseConverter,
    private val messageResponseConverter: MessageResponseConverter,
    private val contentResolver: ContentResolver,
    multipartConverter: IUsedeskMultipartConverter,
    apiFactory: IUsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<RetrofitApi>(
    apiFactory,
    multipartConverter,
    gson,
    RetrofitApi::class.java
), IApiRepository {

    private fun isConnected() = socketApi.isConnected()

    private lateinit var eventListener: EventListener

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
                when {
                    it is UsedeskMessageClient && it.id != it.localId ->
                        eventListener.onMessageUpdated(it)
                    else -> eventListener.onMessagesNewReceived(listOf(it))
                }
            }
        }

        override fun onSetEmailSuccess() {
            eventListener.onSetEmailSuccess()
        }
    }

    override fun initChat(
        configuration: UsedeskChatConfiguration,
        apiToken: String
    ): String {
        val parts = listOf(
            "api_token" to apiToken,
            "company_id" to configuration.companyId,
            "channel_id" to configuration.channelId,
            "name" to configuration.clientName,
            "email" to configuration.clientEmail,
            "phone" to configuration.clientPhoneNumber,
            "additional_id" to configuration.clientAdditionalId,
            "note" to configuration.clientNote,
            "avatar" to getAvatarMultipartBodyPart(configuration),
            "platform" to "sdk"
        )
        val response = doRequestMultipart(
            configuration.urlChatApi,
            parts,
            CreateChatResponse::class.java,
            RetrofitApi::createChat
        )
        return response.token
    }

    override fun connect(
        url: String,
        token: String?,
        configuration: UsedeskChatConfiguration,
        eventListener: EventListener
    ) {
        this.eventListener = eventListener
        runBlocking {
            socketApi.connect(
                url,
                configuration.getInitChatRequest(token),
                socketEventListener
            )
        }
    }

    override fun init(
        configuration: UsedeskChatConfiguration,
        token: String?
    ) {
        socketApi.sendRequest(configuration.getInitChatRequest(token))
    }

    private fun UsedeskChatConfiguration.getInitChatRequest(token: String?) = InitChatRequest(
        token,
        getCompanyAndChannel(),
        urlChat,
        when {
            messagesPageSize > 0 -> messagesPageSize
            else -> null
        }
    )

    override fun convertText(text: String) = messageResponseConverter.convertText(text)

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

        val parts = listOf(
            "chat_token" to token,
            "file" to fileInfo.uri,
            "message_id" to messageId
        )

        doRequestMultipart(
            configuration.urlChatApi,
            parts,
            FileResponse::class.java,
            RetrofitApi::postFile
        )
    }

    override fun setClient(
        configuration: UsedeskChatConfiguration
    ) {
        checkConnection()

        try {
            val parts = listOf(
                "email" to configuration.clientEmail?.getCorrectStringValue(),
                "username" to configuration.clientName?.getCorrectStringValue(),
                "token" to configuration.clientToken,
                "note" to configuration.clientNote,
                "phone" to configuration.clientPhoneNumber,
                "additional_id" to configuration.clientAdditionalId,
                "company_id" to configuration.companyId,
                "avatar" to getAvatarMultipartBodyPart(configuration)
            )
            doRequestMultipart(
                configuration.urlChatApi,
                parts,
                SetClientResponse::class.java,
                RetrofitApi::setClient
            )
            socketEventListener.onSetEmailSuccess()
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        }
    }

    private fun getAvatarMultipartBodyPart(configuration: UsedeskChatConfiguration) =
        if (configuration.clientAvatar != null) {
            try {
                val uri = Uri.parse(configuration.clientAvatar)
                val originalBitmap = contentResolver.openInputStream(uri)
                    .use {
                        BitmapFactory.decodeStream(it)
                    }
                val side = min(originalBitmap.width, originalBitmap.height)
                val outputStream = ByteArrayOutputStream()

                val quadBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    (originalBitmap.width - side) / 2,
                    (originalBitmap.height - side) / 2,
                    side,
                    side
                )
                originalBitmap.recycle()
                val avatarBitmap = quadBitmap.scale(
                    AVATAR_SIZE,
                    AVATAR_SIZE
                )
                quadBitmap.recycle()
                avatarBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    outputStream
                )
                val byteArray = outputStream.toByteArray()
                avatarBitmap.recycle()

                FileBytes(byteArray, configuration.clientAvatar)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null

    override fun loadPreviousMessages(
        configuration: UsedeskChatConfiguration,
        token: String,
        messageId: Long
    ): Boolean {
        val messagesResponse = doRequest(
            configuration.urlChatApi,
            Array<MessageResponse.Message>::class.java
        ) {
            loadPreviousMessages(
                token,
                messageId
            )
        }
        val messages = messagesResponse.flatMap {
            messageResponseConverter.convert(it)
        }
        eventListener.onMessagesOldReceived(messages)
        return messagesResponse.isNotEmpty()
    }

    override fun send(
        configuration: UsedeskChatConfiguration,
        companyId: String,
        offlineForm: UsedeskOfflineForm
    ) {
        try {
            val params = mapOf(
                "email" to offlineForm.clientEmail.getCorrectStringValue(),
                "name" to offlineForm.clientName.getCorrectStringValue(),
                "company_id" to companyId.getCorrectStringValue(),
                "message" to offlineForm.message.getCorrectStringValue(),
                "topic" to offlineForm.topic.getCorrectStringValue()
            )
            val customFields = offlineForm.fields.filter { field ->
                field.value.isNotEmpty()
            }.map { field ->
                field.key to field.value.getCorrectStringValue()
            }
            val json = JsonObject()
            (params + customFields).forEach { param ->
                json.addProperty(param.key, param.value)
            }
            doRequest(
                configuration.urlChatApi,
                OfflineFormResponse::class.java
            ) { sendOfflineForm(json) }
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        }
    }

    override fun send(
        token: String,
        configuration: UsedeskChatConfiguration,
        additionalFields: Map<Long, String>,
        additionalNestedFields: List<Map<Long, String>>
    ): SendResult {
        val totalFields =
            (additionalFields.toList() + additionalNestedFields.flatMap(Map<Long, String>::toList))
                .map { field ->
                    SendAdditionalFieldsRequest.AdditionalField(
                        field.first,
                        field.second
                    )
                }
        val request = SendAdditionalFieldsRequest(token, totalFields)
        val response = doRequest(
            configuration.urlChatApi,
            SendAdditionalFieldsResponse::class.java
        ) { postAdditionalFields(request) }
        return when (response.status) {
            200 -> SendResult.Done
            else -> SendResult.Error
        }
    }

    private fun String.getCorrectStringValue() = replace("\"", "\\\"")

    override fun disconnect() {
        runBlocking {
            socketApi.disconnect()
        }
    }

    private fun checkConnection() {
        if (!isConnected()) {
            throw UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED)
        }
    }

    companion object {
        private val STATUSES_FOR_FORM = listOf(null, 2, 3, 4, 7, 9, 10)

        private const val AVATAR_SIZE = 100
    }
}