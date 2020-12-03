package ru.usedesk.chat_sdk.data.repository.api

import ru.usedesk.chat_sdk._entity.ChatInited
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException

internal interface IApiRepository {
    @Throws(UsedeskException::class)
    fun connect(url: String,
                token: String?,
                configuration: UsedeskChatConfiguration,
                eventListener: EventListener)

    @Throws(UsedeskException::class)
    fun init(configuration: UsedeskChatConfiguration,
             token: String?)

    @Throws(UsedeskException::class)
    fun send(token: String,
             email: String,
             name: String?,
             phone: Long?,
             additionalId: Long?)

    @Throws(UsedeskException::class)
    fun send(configuration: UsedeskChatConfiguration,
             companyId: String,
             offlineForm: UsedeskOfflineForm)

    @Throws(UsedeskException::class)
    fun send(token: String,
             feedback: UsedeskFeedback)

    @Throws(UsedeskException::class)
    fun send(token: String,
             text: String)

    @Throws(UsedeskException::class)
    fun send(token: String,
             usedeskFileInfo: UsedeskFileInfo)

    fun disconnect()


    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)
        fun onChatInited(chatInited: ChatInited)
        fun onNewChatItems(newChatItems: List<UsedeskChatItem>)
        fun onOfflineForm()
    }
}