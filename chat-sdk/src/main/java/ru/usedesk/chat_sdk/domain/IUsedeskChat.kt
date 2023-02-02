package ru.usedesk.chat_sdk.domain

import android.net.Uri
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

interface IUsedeskChat {
    fun addActionListener(listener: IUsedeskActionListener)

    fun removeActionListener(listener: IUsedeskActionListener)

    fun isNoListeners(): Boolean

    fun send(
        textMessage: String,
        localId: Long? = null
    )

    fun send(
        fileInfo: UsedeskFileInfo,
        localId: Long? = null
    )

    fun addFileUploadProgressListener(
        localMessageId: Long,
        listener: IFileUploadProgressListener
    )

    fun removeFileUploadProgressListener(
        localMessageId: Long,
        listener: IFileUploadProgressListener
    )

    fun send(fileInfoList: Collection<UsedeskFileInfo>)

    fun send(
        agentMessage: UsedeskMessageAgentText,
        feedback: UsedeskFeedback
    )

    fun sendAgain(messageId: Long)

    fun removeMessage(messageId: Long)

    fun setMessageDraft(messageDraft: UsedeskMessageDraft)

    fun getMessageDraft(): UsedeskMessageDraft

    fun sendMessageDraft()

    fun send(
        offlineForm: UsedeskOfflineForm,
        onResult: (SendOfflineFormResult) -> Unit
    )

    fun loadPreviousMessagesPage()

    fun loadForm(messageId: Long)

    fun saveForm(form: UsedeskForm)

    fun send(form: UsedeskForm)

    interface IFileUploadProgressListener {
        fun onProgress(sent: Long, total: Long)
    }

    sealed interface SendOfflineFormResult {
        object Done : SendOfflineFormResult
        object Error : SendOfflineFormResult
    }

    data class Model(
        val connectionState: UsedeskConnectionState = UsedeskConnectionState.CONNECTING,
        val clientToken: String = "",
        val messages: List<UsedeskMessage> = listOf(),
        val formMap: Map<Long, UsedeskForm> = mapOf(),
        val previousPageIsAvailable: Boolean = true,
        val previousPageIsLoading: Boolean = false,
        val inited: Boolean = false,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val feedbackEvent: UsedeskSingleLifeEvent<Unit>? = null,
        val thumbnailMap: Map<Long, Uri> = mapOf()
    )
}