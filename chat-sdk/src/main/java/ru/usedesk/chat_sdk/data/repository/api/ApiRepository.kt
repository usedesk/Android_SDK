
package ru.usedesk.chat_sdk.data.repository.api

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.graphics.scale
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.math.min

internal class ApiRepository @Inject constructor(
    private val context: Context,
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
    private val requestDeferredMap = mutableMapOf<Long, CompletableDeferred<SocketSendResponse>>()
    private val mutex = Mutex()

    private val socketEventListener = object : SocketApi.EventListener {
        override fun onConnected() = eventListener.onConnected()

        override fun onDisconnected() {
            runBlocking {
                mutex.withLock {
                    requestDeferredMap.run {
                        values.forEach { it.complete(SocketSendResponse.Error) }
                        clear()
                    }
                }
            }
            eventListener.onDisconnected()
        }

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
            val messages = messageResponseConverter.convert(messageResponse.message)
            val updatedMessages = messages.filter {
                it is UsedeskMessageOwner.Client && it.id != it.localId
            }
            messages.asSequence()
                .filterIsInstance<UsedeskMessageOwner.Client>()
                .mapNotNull { requestDeferredMap[it.localId] }
                .firstOrNull()
                ?.complete(SocketSendResponse.Done)

            val newMessages = messages.filter { it !in updatedMessages }
            if (newMessages.isNotEmpty()) {
                eventListener.onMessagesNewReceived(newMessages)
            }
            updatedMessages.forEach { eventListener.onMessageUpdated(it) }
        }

        override fun onSetEmailSuccess() {
            eventListener.onSetEmailSuccess()
        }
    }

    private fun isConnected() = socketApi.isConnected()

    override suspend fun initChat(
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

    override suspend fun connect(
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

    private fun getUserData(): SocketRequest.Init.Payload.UserData {
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"
        val os = "Android ${Build.VERSION.RELEASE} (API level ${Build.VERSION.SDK_INT})"
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appVersion = packageInfo.versionName
        val appName = packageInfo.packageName
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val mobileOperatorName = telephonyManager?.networkOperatorName ?: ""
        return SocketRequest.Init.Payload.UserData(
            device = device,
            os = os,
            appName = appName,
            appVersion = appVersion,
            mobileOperatorName = mobileOperatorName
        )
    }

    override suspend fun sendInit(
        configuration: UsedeskChatConfiguration,
        token: String?
    ): SocketSendResponse {
        val result = socketApi.sendRequest(configuration.toInitChatRequest(token))
        return result //TODO:
    }

    private fun UsedeskChatConfiguration.toInitChatRequest(
        token: String?
    ) = SocketRequest.Init(
        token,
        companyAndChannel(),
        urlChat,
        when {
            messagesPageSize > 0 -> messagesPageSize
            else -> null
        },
        getUserData()
    )

    override fun convertText(text: String) = messageResponseConverter.convertText(text)

    override suspend fun sendFeedback(
        messageId: Long,
        feedback: UsedeskFeedback
    ): SocketSendResponse {
        val result = socketApi.sendRequest(
            SocketRequest.Feedback(
                messageId,
                when (feedback) {
                    UsedeskFeedback.LIKE -> "LIKE"
                    UsedeskFeedback.DISLIKE -> "DISLIKE"
                }
            )
        )
        return result //TODO:
    }

    override suspend fun sendText(messageText: UsedeskMessage.Text): SocketSendResponse {
        messageText as UsedeskMessageOwner.Client
        val deferred = mutex.withLock {
            CompletableDeferred<SocketSendResponse>().also {
                requestDeferredMap.put(messageText.localId, it)?.complete(SocketSendResponse.Error)
            }
        }
        val result = socketApi.sendRequest(
            SocketRequest.SendMessage(
                messageText.text,
                messageText.id
            )
        )
        return when (result) {
            is SocketSendResponse.Done -> deferred.await()
            is SocketSendResponse.Error -> SocketSendResponse.Error
        }.apply {
            mutex.withLock {
                requestDeferredMap.remove(messageText.localId)
            }
        }
    }

    override suspend fun sendFile(
        configuration: UsedeskChatConfiguration,
        token: String,
        fileInfo: UsedeskFileInfo,
        messageId: Long,
        progressFlow: MutableStateFlow<Pair<Long, Long>>
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
                RetrofitApi::postFile,
                progressFlow
            )
            when (response?.status) {
                200 -> SendFileResponse.Done
                else -> SendFileResponse.Error(response?.code)
            }
        }
        else -> SendFileResponse.Error()
    }

    override suspend fun setClient(configuration: UsedeskChatConfiguration): SetClientResponse =
        when {
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

    private fun String.toFileBytes() = when (this) {
        "" -> null
        else -> try {
            val uri = Uri.parse(this)
            val input = when {
                (uri.scheme ?: "").startsWith("http") -> URL(this).openStream()
                else -> contentResolver.openInputStream(uri)
            }
            val originalBitmap = input.use(BitmapFactory::decodeStream)
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
    }

    override suspend fun loadPreviousMessages(
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
                val messages = response.items.flatMap(messageResponseConverter::convert)
                eventListener.onMessagesOldReceived(messages)
                LoadPreviousMessageResponse.Done(messages)
            }
        }
    }

    override suspend fun sendOfflineForm(
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

    override suspend fun sendFields(
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