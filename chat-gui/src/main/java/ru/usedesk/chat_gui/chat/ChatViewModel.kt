package ru.usedesk.chat_gui.chat

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
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

    private lateinit var actionListenerRx: IUsedeskActionListenerRx
    val configuration = UsedeskChatSdk.requireConfiguration()

    private lateinit var usedeskChat: IUsedeskChat

    fun init() {
        usedeskChat = UsedeskChatSdk.requireInstance()

        clearFileInfoList()
        actionListenerRx = object : IUsedeskActionListenerRx() {

            override fun onConnectedStateObservable(
                    connectedStateObservable: Observable<Boolean>
            ): Disposable? {
                return connectedStateObservable.subscribe {
                    if (!it) {
                        doIt(usedeskChat.connectRx())
                    }
                }
            }

            override fun onNewMessageObservable(
                    newMessageObservable: Observable<UsedeskMessage>
            ): Disposable? {
                return newMessageObservable.subscribe {
                    newMessageLiveData.postValue(it)
                }
            }

            override fun onMessagesObservable(
                    messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.subscribe {
                    messagesLiveData.postValue(it)
                    chatStateLiveData.postValue(ChatState.CHAT)
                }
            }

            override fun onMessageUpdateObservable(
                    messageUpdateObservable: Observable<UsedeskMessage>
            ): Disposable? {
                return messageUpdateObservable.subscribe {
                    messageUpdateLiveData.postValue(it)
                }
            }

            override fun onOfflineFormExpectedObservable(
                    offlineFormExpectedObservable: Observable<UsedeskChatConfiguration>
            ): Disposable? {
                return offlineFormExpectedObservable.subscribe {
                    chatStateLiveData.postValue(ChatState.OFFLINE_FORM)
                }
            }

            override fun onExceptionObservable(
                    exceptionObservable: Observable<Exception>
            ): Disposable? {
                return exceptionObservable.subscribe {
                    exceptionLiveData.postValue(it)
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
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
        UsedeskChatSdk.getInstance()
                ?.removeActionListener(actionListenerRx)
        UsedeskChatSdk.release(false)
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
            doIt(UsedeskChatSdk.requireInstance().sendRx(UsedeskOfflineForm(name, email, message)), {
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