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
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.*
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.EventListener
import ru.usedesk.chat_sdk.data.repository.api.entity.*
import ru.usedesk.chat_sdk.data.repository.api.loader.IInitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.IMessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket.SocketApi
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter.FileBytes
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject
import kotlin.math.min

internal class ApiRepository @Inject constructor(
    private val socketApi: SocketApi,
    private val initChatResponseConverter: IInitChatResponseConverter,
    private val messageResponseConverter: IMessageResponseConverter,
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

    private lateinit var eventListener: EventListener

    private val socketEventListener = object : SocketApi.EventListener {
        override fun onConnected() = eventListener.onConnected()

        override fun onDisconnected() = eventListener.onDisconnected()

        override fun onTokenError() = eventListener.onTokenError()

        override fun onFeedback() = eventListener.onFeedback()

        override fun onException(exception: Exception) = eventListener.onException(exception)

        override fun onInited(initChatResponse: SocketResponse.Inited) {
            initChatResponseConverter.convert(initChatResponse).apply {
                val offlineForm = when (offlineFormSettings.workType) {
                    WorkType.CHECK_WORKING_TIMES -> offlineFormSettings.noOperators
                    WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT ->
                        initChatResponse.setup?.ticket?.statusId in STATUSES_FOR_FORM
                    WorkType.ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT -> true
                    else -> false
                }
                when {
                    offlineForm -> eventListener.onOfflineForm(offlineFormSettings, this)
                    else -> eventListener.onChatInited(this)
                }
            }
        }

        override fun onNew(messageResponse: SocketResponse.AddMessage) {
            val result = messageResponseConverter.convert(messageResponse.message)
            val updatedMessages = result?.messages
                ?.filter { it is UsedeskMessageOwner.Client && it.id != it.localId }
                ?: listOf()
            val newMessages = result?.messages?.filter { it !in updatedMessages } ?: listOf()
            if (newMessages.isNotEmpty()) {
                eventListener.onMessagesNewReceived(newMessages, result?.forms ?: listOf())
            }
            updatedMessages.forEach { eventListener.onMessageUpdated(it) }
        }

        override fun onSetEmailSuccess() {
            eventListener.onSetEmailSuccess()
        }
    }

    private fun isConnected() = socketApi.isConnected()

    override fun initChat(
        configuration: UsedeskChatConfiguration,
        apiToken: String
    ): InitChatResponse {
        val request = CreateChat.Request(
            apiToken,
            configuration.companyId,
            configuration.channelId,
            configuration.clientName,
            configuration.clientEmail,
            configuration.clientPhoneNumber,
            configuration.clientAdditionalId,
            configuration.clientNote,
            configuration.clientAvatar?.toFileBytes()
        )
        val response = doRequestMultipart(
            configuration.urlChatApi,
            request,
            CreateChat.Response::class.java,
            RetrofitApi::createChat
        )
        return when (val clientToken = response?.token) {
            null -> InitChatResponse.ApiError(response?.code)
            else -> InitChatResponse.Done(clientToken)
        }
    }

    override fun connect(
        url: String,
        token: String?,
        configuration: UsedeskChatConfiguration,
        eventListener: EventListener
    ): SocketSendResponse = try {
        this.eventListener = eventListener
        socketApi.connect(
            url,
            configuration.toInitChatRequest(token),
            socketEventListener
        )
        SocketSendResponse.Done
    } catch (e: Exception) {
        eventListener.onException(e)
        SocketSendResponse.Error
    }

    override fun sendInit(
        configuration: UsedeskChatConfiguration,
        token: String?
    ) = socketApi.sendRequest(configuration.toInitChatRequest(token))

    override fun loadForm(
        configuration: UsedeskChatConfiguration,
        form: UsedeskForm
    ): LoadFormResponse {
        val listsId = form.fields
            .asSequence()
            .filterIsInstance<UsedeskMessageAgentText.Field.List>()
            .joinToString(",") { it.id.toString() }
        val request = LoadFields.Request(
            configuration.clientToken!!,
            listsId
        )
        val response = doRequestJson(
            configuration.urlChatApi,
            request,
            LoadFields.Response::class.java,
            RetrofitApi::loadFieldList
        )
        return when (response?.fields) {
            null -> LoadFormResponse.Error(response?.code)
            else -> {
                val fieldMap = form.fields.associateBy(UsedeskMessageAgentText.Field::id)
                val listMap = response.fields
                    .map { it.key to it.value.convertToList(fieldMap) }
                    .toMap()
                val loadedFields = form.fields.mapNotNull {
                    when (it) {
                        is UsedeskMessageAgentText.Field.List -> listMap[it.id.toString()]
                        else -> listOf(it)
                    }
                }.flatten()
                LoadFormResponse.Done(
                    form.copy(
                        fields = loadedFields,
                        state = UsedeskForm.State.LOADED
                    )
                )
            }
        }
    }

    class FieldLoadedList(
        val id: Long,
        val children: Array<Children>
    ) {
        class Children(
            val id: Long,
            val value: String,
            val parentFieldId: Long?
        )
    }

    private fun JsonObject.convertToList(lists: Map<Long, UsedeskMessageAgentText.Field>): List<UsedeskMessageAgentText.Field.List>? =
        valueOrNull {
            when (val list = getAsJsonObject("list")) {
                null -> {
                    val fieldLoaded = gson.fromJson(this, FieldLoadedList::class.java)
                    when {
                        fieldLoaded.children.isEmpty() -> null
                        else -> listOfNotNull(
                            (lists[fieldLoaded.id] as? UsedeskMessageAgentText.Field.List)?.copy(
                                items = fieldLoaded.children.map {
                                    UsedeskMessageAgentText.Field.List.Item(
                                        it.id,
                                        it.value,
                                        it.parentFieldId
                                    )
                                }
                            )
                        )
                    }
                }
                else -> {
                    list.entrySet().mapNotNull {
                        (it.value as JsonObject).convertToList(lists)
                    }.flatten()
                }
            }
        }

    private fun UsedeskChatConfiguration.toInitChatRequest(token: String?) = SocketRequest.Init(
        token,
        companyAndChannel(),
        urlChat,
        when {
            messagesPageSize > 0 -> messagesPageSize
            else -> null
        }
    )

    override fun convertText(text: String) = messageResponseConverter.convertText(text)

    override fun sendFeedback(
        messageId: Long,
        feedback: UsedeskFeedback
    ) = socketApi.sendRequest(
        SocketRequest.Feedback(
            messageId,
            when (feedback) {
                UsedeskFeedback.LIKE -> "LIKE"
                UsedeskFeedback.DISLIKE -> "DISLIKE"
            }
        )
    )

    override fun sendText(messageText: UsedeskMessage.Text) = socketApi.sendRequest(
        SocketRequest.SendMessage(
            messageText.text,
            messageText.id
        )
    )

    override fun sendFile(
        configuration: UsedeskChatConfiguration,
        token: String,
        fileInfo: UsedeskFileInfo,
        messageId: Long
    ): SendFileResponse = when {
        isConnected() -> {
            val request = SendFile.Request(
                token,
                messageId,
                fileInfo.uri
            )

            val response = doRequestMultipart(
                configuration.urlChatApi,
                request,
                SendFile.Response::class.java,
                RetrofitApi::postFile
            )
            when (response?.status) {
                200 -> SendFileResponse.Done
                else -> SendFileResponse.Error(response?.code)
            }
        }
        else -> SendFileResponse.Error()
    }

    override fun setClient(configuration: UsedeskChatConfiguration): SetClientResponse = when {
        isConnected() -> {
            val request = SetClient.Request(
                configuration.clientToken,
                configuration.companyId,
                configuration.clientEmail?.getCorrectStringValue(),
                configuration.clientName?.getCorrectStringValue(),
                configuration.clientNote,
                configuration.clientPhoneNumber,
                configuration.clientAdditionalId,
                configuration.clientAvatar?.toFileBytes()
            )
            val response = doRequestMultipart(
                configuration.urlChatApi,
                request,
                SetClient.Response::class.java,
                RetrofitApi::setClient
            )
            when (response?.clientId) {
                null -> SetClientResponse.Error(response?.code)
                else -> {
                    socketEventListener.onSetEmailSuccess()
                    SetClientResponse.Done
                }
            }
        }
        else -> SetClientResponse.Error()
    }

    private fun String.toFileBytes() = try {
        val uri = Uri.parse(this)
        val originalBitmap = contentResolver.openInputStream(uri)
            .use(BitmapFactory::decodeStream)
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

        FileBytes(byteArray, this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    override fun loadPreviousMessages(
        configuration: UsedeskChatConfiguration,
        token: String,
        messageId: Long
    ): LoadPreviousMessageResponse {
        val request = LoadPreviousMessages.Request(
            token,
            messageId
        )
        val response = doRequestJson(
            configuration.urlChatApi,
            request,
            LoadPreviousMessages.Response::class.java
        ) {
            loadPreviousMessages(
                it.chatToken,
                it.commentId
            )
        }
        return when (response?.items) {
            null -> LoadPreviousMessageResponse.Error(response?.code)
            else -> {
                val results = response.items.mapNotNull(messageResponseConverter::convert)
                val messages = results.flatMap { it.messages ?: listOf() }
                val forms = results.flatMap { it.forms }
                eventListener.onMessagesOldReceived(messages, forms)
                LoadPreviousMessageResponse.Done(messages, forms)
            }
        }
    }

    override fun sendOfflineForm(
        configuration: UsedeskChatConfiguration,
        offlineForm: UsedeskOfflineForm
    ): SendOfflineFormResponse {
        val request = SendOfflineForm.Request(
            offlineForm.clientEmail,
            offlineForm.clientName,
            configuration.companyAndChannel(),
            offlineForm.message.getCorrectStringValue(),
            offlineForm.topic,
            offlineForm.fields.map {
                it.key to it.value.getCorrectStringValue().ifEmpty { null }
            }
        )
        val response = doRequestJsonObject(
            configuration.urlChatApi,
            request,
            SendOfflineForm.Response::class.java,
            RetrofitApi::sendOfflineForm
        )
        return when (response?.status) {
            200 -> SendOfflineFormResponse.Done
            else -> SendOfflineFormResponse.Error(response?.code)
        }
    }

    private fun UsedeskChatConfiguration.companyAndChannel() = "${companyId}_$channelId"

    override fun sendFields(
        token: String,
        configuration: UsedeskChatConfiguration,
        additionalFields: Map<Long, String>,
        additionalNestedFields: List<Map<Long, String>>
    ): SendAdditionalFieldsResponse {
        val totalFields =
            (additionalFields.toList() + additionalNestedFields.flatMap(Map<Long, String>::toList))
                .map { field ->
                    SendAdditionalFields.Request.AdditionalField(
                        field.first,
                        field.second
                    )
                }
        val request = SendAdditionalFields.Request(token, totalFields)
        val response = doRequestJson(
            configuration.urlChatApi,
            request,
            SendAdditionalFields.Response::class.java,
            RetrofitApi::postAdditionalFields
        )
        return when (response?.status) {
            200 -> SendAdditionalFieldsResponse.Done
            else -> SendAdditionalFieldsResponse.Error(response?.code)
        }
    }

    private fun String.getCorrectStringValue() = replace("\"", "\\\"")

    override fun disconnect() {
        runBlocking {
            socketApi.disconnect()
        }
    }

    companion object {
        private val STATUSES_FOR_FORM = listOf(null, 2, 3, 4, 7, 9, 10)

        private const val AVATAR_SIZE = 100
    }
}