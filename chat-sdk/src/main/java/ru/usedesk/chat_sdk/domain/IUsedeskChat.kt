
package ru.usedesk.chat_sdk.domain

import android.net.Uri
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskConnectionState
import ru.usedesk.chat_sdk.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_sdk.entity.UsedeskEvent

interface IUsedeskChat {
    fun addActionListener(listener: IUsedeskActionListener)

    fun removeActionListener(listener: IUsedeskActionListener)

    fun isNoListeners(): Boolean

    fun send(
        textMessage: String,
        localId: String? = null
    )

    fun send(
        fileInfo: UsedeskFileInfo,
        localId: String? = null
    )

    fun addFileUploadProgressListener(
        localMessageId: String,
        listener: IFileUploadProgressListener
    )

    fun removeFileUploadProgressListener(
        localMessageId: String,
        listener: IFileUploadProgressListener
    )

    fun send(fileInfoList: Collection<UsedeskFileInfo>)

    fun send(
        agentMessage: UsedeskMessageAgentText,
        feedback: UsedeskFeedback
    )

    fun sendAgain(messageId: String)

    fun removeMessage(messageId: String)

    fun setMessageDraft(messageDraft: UsedeskMessageDraft)

    fun getMessageDraft(): UsedeskMessageDraft

    fun sendMessageDraft()

    fun send(
        offlineForm: UsedeskOfflineForm,
        onResult: (SendOfflineFormResult) -> Unit
    )

    fun loadPreviousMessagesPage()

    fun loadForm(messageId: String)

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
        val formMap: Map<String, UsedeskForm> = mapOf(),
        val previousPageIsAvailable: Boolean = true,
        val previousPageIsLoading: Boolean = false,
        val inited: Boolean = false,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val feedbackEvent: UsedeskEvent<Unit>? = null,
        val thumbnailMap: Map<String, Uri> = mapOf()
    )
}