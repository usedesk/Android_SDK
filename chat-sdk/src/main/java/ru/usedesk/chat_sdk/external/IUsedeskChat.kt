package ru.usedesk.chat_sdk.external

import io.reactivex.Completable
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageButton
import ru.usedesk.chat_sdk.external.entity.old.UsedeskFeedback
import ru.usedesk.chat_sdk.external.entity.old.UsedeskOfflineForm
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

interface IUsedeskChat {
    @Throws(UsedeskException::class)
    fun connect()

    @Throws(UsedeskException::class)
    fun disconnect()

    @Throws(UsedeskException::class)
    fun send(textMessage: String)

    @Throws(UsedeskException::class)
    fun send(usedeskFileInfo: UsedeskFileInfo)

    @Throws(UsedeskException::class)
    fun send(usedeskFileInfoList: List<UsedeskFileInfo>)

    @Throws(UsedeskException::class)
    fun send(feedback: UsedeskFeedback)

    @Throws(UsedeskException::class)
    fun send(offlineForm: UsedeskOfflineForm)

    @Throws(UsedeskException::class)
    fun send(messageButton: UsedeskMessageButton)

    fun connectRx(): Completable

    fun disconnectRx(): Completable

    fun sendRx(textMessage: String): Completable

    fun sendRx(usedeskFileInfo: UsedeskFileInfo): Completable

    fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable

    fun sendRx(feedback: UsedeskFeedback): Completable

    fun sendRx(offlineForm: UsedeskOfflineForm): Completable

    fun sendRx(messageButton: UsedeskMessageButton): Completable
}