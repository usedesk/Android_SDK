package ru.usedesk.chat_gui.chat.messages

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel

internal class MessagesViewModel : UsedeskViewModel() {

    val modelLiveData = UsedeskLiveData(Model())

    private val actionListenerRx: IUsedeskActionListenerRx
    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    val configuration = UsedeskChatSdk.requireConfiguration()

    private var messages: List<UsedeskMessage> = listOf()

    init {
        setModel { model ->//TODO: ATTENTION!!! NO MAIN THREAD
            model.copy(messageDraft = usedeskChat.getMessageDraft())
        }

        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onMessagesObservable(
                messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.subscribe {
                    messages = it
                    setModel { model ->//TODO: ATTENTION!!! MAY BE NON-MAIN THREAD
                        model.copy(messages = messages)
                    }
                    //messagesLiveData.postValue(messages)
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
    }

    private fun setModel(onUpdate: (Model) -> Model) {
        modelLiveData.value = onUpdate(modelLiveData.value)
    }

    fun onMessageChanged(message: String) {
        setModel { model ->
            model.copy(messageDraft = model.messageDraft.copy(text = message))
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun addAttachedFiles(files: List<UsedeskFileInfo>) {
        setModel { model ->
            model.copy(messageDraft = model.messageDraft.copy(files = files.toSet().toList()))
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
    }

    fun detachFile(file: UsedeskFileInfo) {
        setModel { model ->
            model.copy(
                messageDraft = model.messageDraft.copy(
                    files = model.messageDraft.files - file
                )
            )
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun onSendButton(message: String) {
        doIt(usedeskChat.sendRx(message))
    }

    fun onSend() {
        doIt(usedeskChat.sendMessageDraftRx())

        setModel { model ->
            model.copy(messageDraft = UsedeskMessageDraft())
        }
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
        setModel { model ->
            model.copy(fabToBottom = show)
        }
    }

    data class Model(
        val messageDraft: UsedeskMessageDraft = UsedeskMessageDraft(),
        val fabToBottom: Boolean = false,
        val messages: List<UsedeskMessage> = listOf(),
        val messagesScroll: Long = 0L
    )
}