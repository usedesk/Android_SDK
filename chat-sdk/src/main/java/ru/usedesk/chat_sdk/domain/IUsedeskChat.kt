package ru.usedesk.chat_sdk.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.usedesk.chat_sdk.entity.*

interface IUsedeskChat {
    fun addActionListener(listener: IUsedeskActionListener)

    fun addActionListener(listener: IUsedeskActionListenerRx)

    fun removeActionListener(listener: IUsedeskActionListener)

    fun removeActionListener(listener: IUsedeskActionListenerRx)

    fun isNoListeners(): Boolean

    fun connect()

    fun connectRx(): Completable

    fun disconnect()

    fun disconnectRx(): Completable

    fun send(textMessage: String)

    fun sendRx(textMessage: String): Completable

    fun send(usedeskFileInfoList: List<UsedeskFileInfo>)

    fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable

    fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback)

    fun sendRx(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback): Completable

    fun send(offlineForm: UsedeskOfflineForm)

    fun sendRx(offlineForm: UsedeskOfflineForm): Completable

    fun sendAgain(id: Long)

    fun sendAgainRx(id: Long): Completable

    fun removeMessage(id: Long)

    fun removeMessageRx(id: Long): Completable

    fun setMessageDraft(messageDraft: UsedeskMessageDraft)

    fun setMessageDraftRx(messageDraft: UsedeskMessageDraft): Completable

    fun getMessageDraft(): UsedeskMessageDraft

    fun getMessageDraftRx(): Single<UsedeskMessageDraft>

    fun sendMessageDraft()

    fun sendMessageDraftRx(): Completable

    /**
     * @return
     * true - can load next messages page
     */
    fun loadPreviousMessagesPage(): Boolean

    fun release()

    fun releaseRx(): Completable
}