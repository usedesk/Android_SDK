package ru.usedesk.chat_gui.internal.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.external.entity.ticketitem.ChatItem
import ru.usedesk.chat_sdk.external.entity.ticketitem.MessageFile
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import java.util.*

class ChatViewModel : ViewModel() {

    private val disposables = CompositeDisposable()
    val feedbacksLiveData = MutableLiveData<Set<Int>>()
    val exceptionLiveData = MutableLiveData<UsedeskException>()
    val messagePanelStateLiveData = MutableLiveData(MessagePanelState.MESSAGE_PANEL)
    val messagesLiveData = MutableLiveData<List<UsedeskMessage>>()
    val fileInfoListLiveData = MutableLiveData<List<UsedeskFileInfo>>()
    val messageLiveData = MutableLiveData("")
    val nameLiveData = MutableLiveData("")
    val emailLiveData = MutableLiveData("")
    val ticketItemsLiveData = MutableLiveData<List<ChatItem>>()

    val actionListenerRx = UsedeskActionListenerRx()

    private lateinit var usedeskChat: IUsedeskChat

    fun init() {
        usedeskChat = UsedeskChatSdk.getInstance()

        clearFileInfoList()
        toLiveData(actionListenerRx.getTicketItemsObservable(), ticketItemsLiveData)
        toLiveData(actionListenerRx.messagesObservable, messagesLiveData)
        toLiveData(actionListenerRx.offlineFormExpectedObservable.map {
            nameLiveData.postValue(it.clientName)
            emailLiveData.postValue(it.email)
            MessagePanelState.OFFLINE_FORM_EXPECTED
        }, messagePanelStateLiveData)
        toLiveData(actionListenerRx.exceptionObservable, exceptionLiveData)
        disposables.add(actionListenerRx.getConnectedStateSubject().subscribe {
            if (!it) {
                justComplete(usedeskChat.connectRx())
            }
        })
        feedbacksLiveData.value = HashSet()
    }

    fun onMessageChanged(message: String) {
        messageLiveData.value = message
    }

    private fun clearFileInfoList() {
        fileInfoListLiveData.value = ArrayList()
    }

    private fun justComplete(completable: Completable) {
        addDisposable(completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}) { it.printStackTrace() })
    }

    private fun <OUT : IN?, IN> toLiveData(observable: Observable<OUT>, liveData: MutableLiveData<IN>) {
        addDisposable(observable.subscribe({ liveData.postValue(it) }) { it.printStackTrace() })
    }

    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun onClickFile(messageFile: MessageFile) {
        //TODO:
    }

    fun onShowHtmlClick(html: String) {
        //TODO:
    }

    fun setAttachedFiles(usedeskFileInfoList: List<UsedeskFileInfo>) {
        fileInfoListLiveData.postValue(usedeskFileInfoList)
    }

    fun sendFeedback(messageIndex: Int, feedback: UsedeskFeedback) {
        val feedbacks: MutableSet<Int> = HashSet(feedbacksLiveData.value!!.size + 1)
        feedbacks.addAll(feedbacksLiveData.value!!)
        feedbacks.add(messageIndex)
        feedbacksLiveData.postValue(feedbacks)
        justComplete(usedeskChat.sendRx(feedback))
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        UsedeskChatSdk.release()
    }

    fun detachFile(usedeskFileInfo: UsedeskFileInfo) {
        val attachedFileInfoList: MutableList<UsedeskFileInfo> = fileInfoListLiveData.value?.toMutableList()
                ?: mutableListOf()
        attachedFileInfoList.remove(usedeskFileInfo)
        setAttachedFiles(attachedFileInfoList)
    }

    fun onSend(textMessage: String) {
        justComplete(usedeskChat.sendRx(textMessage))
        justComplete(usedeskChat.sendRx(fileInfoListLiveData.value))
        clearFileInfoList()
    }

    fun onSend(name: String, email: String, message: String) {
        justComplete(usedeskChat.sendRx(UsedeskOfflineForm(null, name, email, message)).doOnComplete {
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