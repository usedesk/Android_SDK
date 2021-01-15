package ru.usedesk.chat_gui.chat

import androidx.lifecycle.MutableLiveData
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

internal class ChatViewModel : UsedeskViewModel() {

    val exceptionLiveData = MutableLiveData<Exception>()
    val fileInfoListLiveData = MutableLiveData<List<UsedeskFileInfo>>()
    val messageLiveData = MutableLiveData("")
    val messagesLiveData = MutableLiveData<List<UsedeskMessage>>(listOf())
    val newMessageLiveData = MutableLiveData<UsedeskMessage>()
    val messageUpdateLiveData = MutableLiveData<UsedeskMessage>()
    val chatStateLiveData = MutableLiveData(ChatState.LOADING)

    val offlineFormStateLiveData = MutableLiveData(OfflineFormState.DEFAULT)
    val nameErrorLiveData = MutableLiveData(false)
    val emailErrorLiveData = MutableLiveData(false)
    val messageErrorLiveData = MutableLiveData(false)

    val actionListenerRx = UsedeskActionListenerRx()
    val configuration = UsedeskChatSdk.requireConfiguration()

    private lateinit var usedeskChat: IUsedeskChat

    fun init() {
        usedeskChat = UsedeskChatSdk.getInstance()

        clearFileInfoList()
        addDisposable(actionListenerRx.messagesObservable.subscribe {
            messagesLiveData.postValue(it.reversed())
            chatStateLiveData.postValue(ChatState.CHAT)
        })
        addDisposable(actionListenerRx.newMessageObservable.subscribe {
            newMessageLiveData.postValue(it)
        })
        addDisposable(actionListenerRx.offlineFormExpectedObservable.subscribe {
            chatStateLiveData.postValue(ChatState.OFFLINE_FORM)
        })
        addDisposable(actionListenerRx.exceptionObservable.subscribe {
            exceptionLiveData.postValue(it)
        })
        addDisposable(actionListenerRx.connectedStateObservable.subscribe {

            if (!it) {
                doIt(usedeskChat.connectRx())
            }
        })
        addDisposable(actionListenerRx.messageUpdateObservable.subscribe {
            messageUpdateLiveData.postValue(it)
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

    fun onSend(name: String,
               email: String,
               message: String) {
        val nameIsValid = name.isNotEmpty()
        val emailIsValid = UsedeskValidatorUtil.isValidEmailNecessary(email)
        val messageIsValid = message.isNotEmpty()

        if (nameIsValid && emailIsValid && messageIsValid) {
            offlineFormStateLiveData.postValue(OfflineFormState.SENDING)
            doIt(UsedeskChatSdk.getInstance().sendRx(UsedeskOfflineForm(name, email, message)), {
                offlineFormStateLiveData.postValue(OfflineFormState.SENT_SUCCESSFULLY)
            }) {
                offlineFormStateLiveData.postValue(OfflineFormState.FAILED_TO_SEND)
            }
        } else {
            messageErrorLiveData.value = !messageIsValid
            emailErrorLiveData.value = !emailIsValid
            nameErrorLiveData.value = !nameIsValid
        }
    }

    fun onOfflineFormNameChanged() {
        nameErrorLiveData.value = false
    }

    fun onOfflineFormEmailChanged() {
        emailErrorLiveData.value = false
    }

    fun onOfflineFormMessageChanged() {
        messageErrorLiveData.value = false
    }

    enum class ChatState {
        LOADING,
        CHAT,
        OFFLINE_FORM
    }

    enum class OfflineFormState {
        DEFAULT,
        SENDING,
        SENT_SUCCESSFULLY,
        FAILED_TO_SEND
    }
}