package ru.usedesk.chat_gui.internal.chat

import androidx.lifecycle.MutableLiveData
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.external.entity.old.UsedeskOfflineForm
import ru.usedesk.common_gui.internal.UsedeskViewModel

class ChatViewModel : UsedeskViewModel() {

    val exceptionLiveData = MutableLiveData<Exception>()
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

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
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