package ru.usedesk.chat_gui.chat.messages

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskViewModel

internal class MessagesViewModel : UsedeskViewModel() {

    val fileInfoListLiveData = MutableLiveData<List<UsedeskFileInfo>?>(listOf())
    val messagesLiveData = MutableLiveData<List<UsedeskMessage>?>(listOf())
    val fabToBottomLiveData = MutableLiveData<Boolean?>(false)
    var message = ""
        private set

    private val actionListenerRx: IUsedeskActionListenerRx
    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    val configuration = UsedeskChatSdk.requireConfiguration()

    private var messages: List<UsedeskMessage> = listOf()

    init {
        clearFileInfoList()
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
    }

    fun onMessageChanged(message: String) {
        this.message = message
    }

    private fun clearFileInfoList() {
        fileInfoListLiveData.value = listOf()
    }

    fun getAttachedFiles(): List<UsedeskFileInfo> {
        return fileInfoListLiveData.value ?: listOf()
    }

    fun setAttachedFiles(usedeskFileInfoList: List<UsedeskFileInfo>) {
        val attached = (fileInfoListLiveData.value
            ?: listOf()) + usedeskFileInfoList
        fileInfoListLiveData.postValue(attached.distinct())
    }

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
    }

    fun detachFile(usedeskFileInfo: UsedeskFileInfo) {
        val attachedFileInfoList: MutableList<UsedeskFileInfo> =
            fileInfoListLiveData.value?.toMutableList()
                ?: mutableListOf()
        attachedFileInfoList.remove(usedeskFileInfo)
        fileInfoListLiveData.postValue(attachedFileInfoList)
    }

    fun onSend(textMessage: String) {
        doIt(usedeskChat.sendRx(textMessage))
        fileInfoListLiveData.value?.also {
            doIt(usedeskChat.sendRx(it))
        }
        clearFileInfoList()
    }

    fun onSendAgain(id: Long) {
        doIt(usedeskChat.sendAgainRx(id))
    }

    override fun onCleared() {
        super.onCleared()
        UsedeskChatSdk.getInstance()
            ?.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }

    fun showToBottomButton(show: Boolean) {

    }
}