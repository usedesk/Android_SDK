package ru.usedesk.chat_sdk.data.repository.api

import ru.usedesk.chat_sdk.data.repository.api.entity.ChatInited
import ru.usedesk.chat_sdk.entity.*

internal interface IApiRepository {
    fun connect(url: String,
                token: String?,
                configuration: UsedeskChatConfiguration,
                eventListener: EventListener)

    fun init(configuration: UsedeskChatConfiguration,
             token: String?)

    fun send(token: String?,
             signature: String?,
             email: String?,
             name: String?,
             note: String?,
             phone: Long?,
             additionalId: Long?)

    fun send(configuration: UsedeskChatConfiguration,
             companyId: String,
             offlineForm: UsedeskOfflineForm)

    fun send(token: String,
             messageId: Long,
             feedback: UsedeskFeedback)

    fun send(token: String,
             text: String)

    fun send(configuration: UsedeskChatConfiguration,
             token: String,
             fileInfo: UsedeskFileInfo)

    fun disconnect()

    fun sendAgain(messageClient: UsedeskMessageClient)

    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)
        fun onChatInited(chatInited: ChatInited)
        fun onMessagesReceived(newMessages: List<UsedeskMessage>)
        fun onMessageUpdated(message: UsedeskMessage)
        fun onOfflineForm()
        fun onSetEmailSuccess()
    }
}