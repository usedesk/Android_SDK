
package ru.usedesk.chat_sdk.data.repository.api

import androidx.annotation.CheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import ru.usedesk.chat_sdk.entity.ChatInited
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings

internal interface ChatApi {
    @CheckResult
    suspend fun connect(
        configuration: UsedeskChatConfiguration,
        eventListener: EventListener
    ): SocketSendResponse

    @CheckResult
    suspend fun sendInit(
        configuration: UsedeskChatConfiguration,
    ): SocketSendResponse

    @CheckResult
    suspend fun sendOfflineForm(
        configuration: UsedeskChatConfiguration,
        offlineForm: UsedeskOfflineForm
    ): SendOfflineFormResponse

    @CheckResult
    suspend fun sendFeedback(
        messageId: String,
        feedback: UsedeskFeedback
    ): SocketSendResponse

    @CheckResult
    suspend fun sendText(messageText: UsedeskMessage.Text): SocketSendResponse

    @CheckResult
    suspend fun sendFile(
        configuration: UsedeskChatConfiguration,
        fileInfo: UsedeskFileInfo,
        messageId: String,
        progressFlow: MutableStateFlow<Pair<Long, Long>>
    ): SendFileResponse

    suspend fun setClient(configuration: UsedeskChatConfiguration): SetClientResponse

    @CheckResult
    suspend fun sendFields(
        configuration: UsedeskChatConfiguration,
    ): SendAdditionalFieldsResponse

    @CheckResult
    suspend fun loadPreviousMessages(
        configuration: UsedeskChatConfiguration,
        messageId: String
    ): LoadPreviousMessageResponse

    fun disconnect()

    fun convertText(text: String): String

    @CheckResult
    suspend fun initChat(
        configuration: UsedeskChatConfiguration,
        apiToken: String
    ): InitChatResponse

    sealed interface SendFileResponse {
        object Done : SendFileResponse
        class Error(val error: Int? = null) : SendFileResponse
    }

    sealed interface SendOfflineFormResponse {
        object Done : SendOfflineFormResponse
        class Error(val error: Int? = null) : SendOfflineFormResponse
    }

    sealed interface SocketSendResponse {
        object Done : SocketSendResponse
        object Error : SocketSendResponse
    }

    sealed interface InitChatResponse {
        class Done(val clientToken: String) : InitChatResponse
        class ApiError(val code: Int?) : InitChatResponse
    }

    sealed interface SetClientResponse {
        object Done : SetClientResponse
        class Error(val error: Int? = null) : SetClientResponse
    }

    sealed interface LoadPreviousMessageResponse {
        class Done(val messages: List<UsedeskMessage>) : LoadPreviousMessageResponse
        class Error(val code: Int?) : LoadPreviousMessageResponse
    }

    sealed interface SendAdditionalFieldsResponse {
        object Done : SendAdditionalFieldsResponse
        class Error(val code: Int? = null) : SendAdditionalFieldsResponse
    }

    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)
        fun onChatInited(chatInited: ChatInited)
        fun onMessagesOldReceived(messages: List<UsedeskMessage>)
        fun onMessagesNewReceived(messages: List<UsedeskMessage>)
        fun onMessageUpdated(message: UsedeskMessage)
        fun onOfflineForm(offlineFormSettings: UsedeskOfflineFormSettings, chatInited: ChatInited)
        fun onSetEmailSuccess()
    }
}