package ru.usedesk.chat_gui.internal.chat

import androidx.lifecycle.MutableLiveData
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.external.entity.chat.UsedeskChatItem
import ru.usedesk.common_gui.internal.UsedeskViewModel
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import java.util.*

class ChatViewModel : UsedeskViewModel() {

    val feedbacksLiveData = MutableLiveData<Set<Int>>()
    val exceptionLiveData = MutableLiveData<UsedeskException>()
    val messagePanelStateLiveData = MutableLiveData(MessagePanelState.MESSAGE_PANEL)
    val fileInfoListLiveData = MutableLiveData<List<UsedeskFileInfo>>()
    val messageLiveData = MutableLiveData("")
    val nameLiveData = MutableLiveData("")
    val emailLiveData = MutableLiveData("")
    val chatItemsLiveData = MutableLiveData<List<UsedeskChatItem>>()

    val actionListenerRx = UsedeskActionListenerRx()

    private lateinit var usedeskChat: IUsedeskChat

    fun init() {
        usedeskChat = UsedeskChatSdk.getInstance()

        clearFileInfoList()
        addDisposable(actionListenerRx.chatItemsObservable.subscribe {
            chatItemsLiveData.postValue(it.reversed())
        })
        addDisposable(actionListenerRx.offlineFormExpectedObservable.subscribe {
            nameLiveData.postValue(it.clientName)
            emailLiveData.postValue(it.email)
            messagePanelStateLiveData.postValue(MessagePanelState.OFFLINE_FORM_EXPECTED)
        })
        addDisposable(actionListenerRx.exceptionObservable.subscribe {
            exceptionLiveData.postValue(it)
        })
        addDisposable(actionListenerRx.connectedStateObservable.subscribe {
            if (!it) {
                doIt(usedeskChat.connectRx())
            }
        })
        feedbacksLiveData.value = HashSet()
    }

    fun onMessageChanged(message: String) {
        messageLiveData.value = message
    }

    private fun clearFileInfoList() {
        fileInfoListLiveData.value = listOf()
    }

    fun setAttachedFiles(usedeskFileInfoList: List<UsedeskFileInfo>) {
        fileInfoListLiveData.postValue(usedeskFileInfoList)
    }

    fun sendFeedback(messageIndex: Int, feedback: UsedeskFeedback) {
        val feedbacks: MutableSet<Int> = HashSet(feedbacksLiveData.value!!.size + 1)
        feedbacks.addAll(feedbacksLiveData.value!!)
        feedbacks.add(messageIndex)
        feedbacksLiveData.postValue(feedbacks)
        doIt(usedeskChat.sendRx(feedback))
    }

    override fun onCleared() {
        super.onCleared()
        UsedeskChatSdk.release()
    }

    fun detachFile(usedeskFileInfo: UsedeskFileInfo) {
        val attachedFileInfoList: MutableList<UsedeskFileInfo> = fileInfoListLiveData.value?.toMutableList()
                ?: mutableListOf()
        attachedFileInfoList.remove(usedeskFileInfo)
        setAttachedFiles(attachedFileInfoList)
    }

    fun onSend(textMessage: String) {
        doIt(usedeskChat.sendRx(textMessage))
        fileInfoListLiveData.value?.also {
            doIt(usedeskChat.sendRx(it))
        }
        clearFileInfoList()
    }

    fun onSend(name: String, email: String, message: String) {
        doIt(usedeskChat.sendRx(UsedeskOfflineForm(null, name, email, message)).doOnComplete {
            messagePanelStateLiveData.postValue(MessagePanelState.OFFLINE_FORM_SENT)
        })
    }

    fun onNameChanged(name: String) {
        nameLiveData.value = name
    }

    fun onEmailChanged(email: String) {
        emailLiveData.value = email
    }
}