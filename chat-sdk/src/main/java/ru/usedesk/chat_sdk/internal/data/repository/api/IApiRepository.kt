package ru.usedesk.chat_sdk.internal.data.repository.api

import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

interface IApiRepository {
    @Throws(UsedeskException::class)
    fun connect(url: String,
                onMessageListener: OnMessageListener)

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
}