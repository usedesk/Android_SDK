package ru.usedesk.chat_sdk.domain

import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

interface IUsedeskChat {
    fun addActionListener(listener: IUsedeskActionListener)

    fun removeActionListener(listener: IUsedeskActionListener)

    fun isNoListeners(): Boolean

    fun connect()

    fun disconnect()

    fun loadForm(messageId: Long)

    fun send(textMessage: String)

    fun send(usedeskFileInfoList: List<UsedeskFileInfo>)

    fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback)

    fun send(offlineForm: UsedeskOfflineForm)

    fun sendAgain(id: Long)

    fun removeMessage(id: Long)

    fun setMessageDraft(messageDraft: UsedeskMessageDraft)

    fun getMessageDraft(): UsedeskMessageDraft

    fun sendMessageDraft()

    fun createChat(apiToken: String): CreateChatResult

    sealed interface CreateChatResult {
        class Done(val clientToken: String) : CreateChatResult
        class Error(val code: Int?) : CreateChatResult
    }

    /**
     * @return
     * true - can load next messages page
     */
    fun loadPreviousMessagesPage(): Boolean

    fun release()

    data class Model(
        val connectionState: UsedeskConnectionState = UsedeskConnectionState.CONNECTING,
        val clientToken: String? = null,
        val messages: List<UsedeskMessage> = listOf(),
        val inited: Boolean = false,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val feedbackEvent: UsedeskSingleLifeEvent<Unit>? = null
    )
}