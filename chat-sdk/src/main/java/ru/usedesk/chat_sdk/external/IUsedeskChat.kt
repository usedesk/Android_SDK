package ru.usedesk.chat_sdk.external

import io.reactivex.Completable
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageAgentText
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
    fun send(message: UsedeskMessageAgentText, feedback: UsedeskFeedback)

    @Throws(UsedeskException::class)
    fun send(offlineForm: UsedeskOfflineForm)

    fun connectRx(): Completable

    fun disconnectRx(): Completable

    fun sendRx(textMessage: String): Completable

    fun sendRx(usedeskFileInfo: UsedeskFileInfo): Completable

    fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable

    fun sendRx(message: UsedeskMessageAgentText, feedback: UsedeskFeedback): Completable

    fun sendRx(offlineForm: UsedeskOfflineForm): Completable
}