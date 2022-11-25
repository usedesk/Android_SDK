package ru.usedesk.chat_sdk.data.repository.api

import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form.Field

internal interface IApiRepository {
    fun connect(
        url: String,
        token: String?,
        configuration: UsedeskChatConfiguration,
        eventListener: EventListener
    ): SocketSendResponse

    fun sendInit(
        configuration: UsedeskChatConfiguration,
        token: String?
    ): SocketSendResponse

    fun sendOfflineForm(
        configuration: UsedeskChatConfiguration,
        offlineForm: UsedeskOfflineForm
    ): SendOfflineFormResponse

    fun sendFeedback(
        messageId: Long,
        feedback: UsedeskFeedback
    ): SocketSendResponse

    fun sendText(messageText: UsedeskMessageText): SocketSendResponse

    fun sendFile(
        configuration: UsedeskChatConfiguration,
        token: String,
        fileInfo: UsedeskFileInfo,
        messageId: Long
    ): SendFileResponse

    fun setClient(configuration: UsedeskChatConfiguration): SetClientResponse

    fun send(
        token: String,
        configuration: UsedeskChatConfiguration,
        additionalFields: Map<Long, String>,
        additionalNestedFields: List<Map<Long, String>>
    ): SendAdditionalFieldsResponse

    fun loadPreviousMessages(
        configuration: UsedeskChatConfiguration,
        token: String,
        messageId: Long
    ): LoadPreviousMessageResponse

    fun disconnect()

    fun convertText(text: String): String

    fun initChat(
        configuration: UsedeskChatConfiguration,
        apiToken: String
    ): InitChatResponse

    fun loadForm(
        configuration: UsedeskChatConfiguration,
        fields: List<Field.List>
    ): LoadFormResponse

    interface LoadFormResponse {
        class Done(val fields: List<Field>) : LoadFormResponse
        class Error(val error: Int? = null) : LoadFormResponse
    }

    interface SendFileResponse {
        object Done : SendFileResponse
        class Error(val error: Int? = null) : SendFileResponse
    }

    interface SendOfflineFormResponse {
        object Done : SendOfflineFormResponse
        class Error(val error: Int? = null) : SendOfflineFormResponse
    }


    interface SocketSendResponse {
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
        object Error : SendAdditionalFieldsResponse
    }

    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)
        fun onChatInited(chatInited: ChatInited)
        fun onMessagesOldReceived(oldMessages: List<UsedeskMessage>)
        fun onMessagesNewReceived(newMessages: List<UsedeskMessage>)
        fun onMessageUpdated(message: UsedeskMessage)
        fun onOfflineForm(offlineFormSettings: UsedeskOfflineFormSettings, chatInited: ChatInited)
        fun onSetEmailSuccess()
    }
}