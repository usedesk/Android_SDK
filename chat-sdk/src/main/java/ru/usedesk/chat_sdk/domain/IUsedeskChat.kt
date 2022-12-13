package ru.usedesk.chat_sdk.domain

import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

interface IUsedeskChat {
    fun addActionListener(listener: IUsedeskActionListener)

    fun removeActionListener(listener: IUsedeskActionListener)

    fun isNoListeners(): Boolean

    fun loadForm(messageId: Long)

    fun send(textMessage: String)

    fun send(usedeskFileInfoList: List<UsedeskFileInfo>)

    fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback)

    fun sendAgain(messageId: Long)

    fun removeMessage(messageId: Long)

    fun setMessageDraft(messageDraft: UsedeskMessageDraft)

    fun getMessageDraft(): UsedeskMessageDraft

    fun sendMessageDraft()

    fun send(offlineForm: UsedeskOfflineForm, onResult: (SendOfflineFormResult) -> Unit)

    fun createChat(apiToken: String, onResult: (CreateChatResult) -> Unit)

    fun loadPreviousMessagesPage()

    sealed interface SendOfflineFormResult {
        object Done : SendOfflineFormResult
        object Error : SendOfflineFormResult
    }

    sealed interface CreateChatResult {
        class Done(val clientToken: String) : CreateChatResult
        object Error : CreateChatResult
    }

    data class Model(
        val connectionState: UsedeskConnectionState = UsedeskConnectionState.CONNECTING,
        val clientToken: String = "",
        val messages: List<UsedeskMessage> = listOf(),
        val previousPageIsAvailable: Boolean = true,
        val previousPageIsLoading: Boolean = false,
        val inited: Boolean = false,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val feedbackEvent: UsedeskSingleLifeEvent<Unit>? = null,
        val formLoadSet: Set<Long> = setOf()
    )
}