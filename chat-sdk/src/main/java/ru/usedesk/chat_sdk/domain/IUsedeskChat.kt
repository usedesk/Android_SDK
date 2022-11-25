package ru.usedesk.chat_sdk.domain

import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

interface IUsedeskChat {
    //TODO: сделать все методы асинхронными
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

    fun createChat(apiToken: String, onResult: (CreateChatResult) -> Unit)

    fun loadPreviousMessagesPage()

    fun release()

    sealed interface CreateChatResult {
        class Done(val clientToken: String) : CreateChatResult
        class Error(val code: Int?) : CreateChatResult
    }

    data class Model(
        val connectionState: UsedeskConnectionState = UsedeskConnectionState.DISCONNECTED,
        val clientToken: String? = null,
        val messages: List<UsedeskMessage> = listOf(),
        val previousPageIsAvailable: Boolean = true,
        val previousPageIsLoading: Boolean = false,
        val inited: Boolean = false,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val feedbackEvent: UsedeskSingleLifeEvent<Unit>? = null
    )
}