package ru.usedesk.chat_gui.chat.messages

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel

internal class MessagesViewModel : UsedeskViewModel() {

    val messagesLiveData = MutableLiveData<List<UsedeskMessage>?>(listOf())
    val fabToBottomLiveData = MutableLiveData<Boolean?>(false)
    val messageDraftLiveData = UsedeskLiveData(UsedeskMessageDraft())

    private val actionListenerRx: IUsedeskActionListenerRx
    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    val configuration = UsedeskChatSdk.requireConfiguration()

    private var messages: List<UsedeskMessage> = listOf()

    init {
        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onMessagesObservable(
                messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.subscribe {
                    messages = it
                    messagesLiveData.postValue(messages)
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
        messageDraftLiveData.value = usedeskChat.getMessageDraft()
    }

    fun onMessageChanged(message: String) {
        messageDraftLiveData.value = messageDraftLiveData.value.copy(
            text = message
        )
        doIt(usedeskChat.setMessageDraftRx(messageDraftLiveData.value))
    }

    fun addAttachedFiles(files: List<UsedeskFileInfo>) {
        val messageDraft = messageDraftLiveData.value
        messageDraftLiveData.value = messageDraft.copy(
            files = (messageDraft.files + files).toSet().toList()
        )
        doIt(usedeskChat.setMessageDraftRx(messageDraftLiveData.value))
    }

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
    }

    fun detachFile(file: UsedeskFileInfo) {
        val messageDraft = messageDraftLiveData.value
        messageDraftLiveData.value = messageDraft.copy(
            files = messageDraft.files.filter {
                it != file
            }
        )
        doIt(usedeskChat.setMessageDraftRx(messageDraftLiveData.value))
    }

    fun onSendButton(message: String) {
        doIt(usedeskChat.sendRx(message))
    }

    fun onSend() {
        doIt(usedeskChat.sendMessageDraftRx())

        messageDraftLiveData.value = UsedeskMessageDraft()
    }

    fun sendAgain(id: Long) {
        doIt(usedeskChat.sendAgainRx(id))
    }

    fun removeMessage(id: Long) {
        doIt(usedeskChat.removeMessageRx(id))
    }

    override fun onCleared() {
        super.onCleared()
        UsedeskChatSdk.getInstance()
            ?.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }

    fun showToBottomButton(show: Boolean) {
        fabToBottomLiveData.value = show
    }
}