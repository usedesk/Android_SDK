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
        token: String,
        email: String?,
        name: String?,
        note: String?,
        phone: Long?,
        additionalId: String?
    )

    fun send(
        configuration: UsedeskChatConfiguration,
        companyId: String,
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

    fun send(
        token: String?,
        configuration: UsedeskChatConfiguration,
        additionalFields: Map<Long, String>,
        additionalNestedFields: List<Map<Long, String>>
    )

    fun disconnect()

    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)
        fun onChatInited(chatInited: ChatInited)
        fun onMessagesReceived(newMessages: List<UsedeskMessage>)
        fun onMessageUpdated(message: UsedeskMessage)
        fun onOfflineForm(offlineFormSettings: UsedeskOfflineFormSettings, chatInited: ChatInited)
        fun onSetEmailSuccess()
    }
}