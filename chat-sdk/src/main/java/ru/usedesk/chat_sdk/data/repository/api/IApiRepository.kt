package ru.usedesk.chat_sdk.data.repository.api

import ru.usedesk.chat_sdk.entity.*

internal interface IApiRepository {
    fun connect(
        url: String,
        token: String?,
        configuration: UsedeskChatConfiguration,
        eventListener: EventListener
    )

    fun init(
        configuration: UsedeskChatConfiguration,
        token: String?
    )

    fun send(
        configuration: UsedeskChatConfiguration,
        offlineForm: UsedeskOfflineForm
    )

    fun send(
        messageId: Long,
        feedback: UsedeskFeedback
    )

    fun send(messageText: UsedeskMessageText)

    fun send(
        configuration: UsedeskChatConfiguration,
        token: String,
        fileInfo: UsedeskFileInfo,
        messageId: Long
    )

    fun setClient(configuration: UsedeskChatConfiguration)

    fun send(
        token: String,
        configuration: UsedeskChatConfiguration,
        additionalFields: Map<Long, String>,
        additionalNestedFields: List<Map<Long, String>>
    ): SendResult

    fun loadPreviousMessages(
        configuration: UsedeskChatConfiguration,
        token: String,
        messageId: Long
    ): Boolean

    fun disconnect()

    fun convertText(text: String): String

    fun initChat(
        configuration: UsedeskChatConfiguration,
        apiToken: String
    ): String

    sealed interface SendResult {
        object Done : SendResult
        object Error : SendResult
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