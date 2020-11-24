package ru.usedesk.chat_gui.internal.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.release
import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import java.util.*

class ChatViewModel internal constructor(private val usedeskChat: IUsedeskChat, actionListenerRx: UsedeskActionListenerRx) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val feedbacksLiveData = MutableLiveData<Set<Int>>()
    private val exceptionLiveData = MutableLiveData<UsedeskException>()
    private val messagePanelStateLiveData = MutableLiveData(MessagePanelState.MESSAGE_PANEL)
    private val messagesLiveData = MutableLiveData<List<UsedeskMessage>>()
    private val fileInfoListLiveData = MutableLiveData<List<UsedeskFileInfo>>()
    private val messageLiveData = MutableLiveData("")
    private val nameLiveData = MutableLiveData("")
    private val emailLiveData = MutableLiveData("")
    fun onMessageChanged(message: String) {
        messageLiveData.value = message
    }

    private fun clearFileInfoList() {
        fileInfoListLiveData.value = ArrayList()
    }

    private fun justComplete(completable: Completable) {
        addDisposable(completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}) { obj: Throwable -> obj.printStackTrace() })
    }

    private fun <OUT : IN?, IN> toLiveData(observable: Observable<OUT>, liveData: MutableLiveData<IN>) {
        addDisposable(observable.subscribe({ value: OUT -> liveData.postValue(value) }) { obj: Throwable -> obj.printStackTrace() })
    }

    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun getFeedbacksLiveData(): LiveData<Set<Int>> {
        return feedbacksLiveData
    }

    fun getExceptionLiveData(): LiveData<UsedeskException> {
        return exceptionLiveData
    }

    fun getMessagePanelStateLiveData(): LiveData<MessagePanelState> {
        return messagePanelStateLiveData
    }

    fun getMessageLiveData(): LiveData<String> {
        return messageLiveData
    }

    fun getNameLiveData(): LiveData<String?> {
        return nameLiveData
    }

    fun getEmailLiveData(): LiveData<String> {
        return emailLiveData
    }

    fun getMessagesLiveData(): LiveData<List<UsedeskMessage>> {
        return messagesLiveData
    }

    fun getFileInfoListLiveData(): LiveData<List<UsedeskFileInfo>> {
        return fileInfoListLiveData
    }

    fun setAttachedFileInfoList(usedeskFileInfoList: List<UsedeskFileInfo>) {
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
        release()
    }

    fun detachFile(usedeskFileInfo: UsedeskFileInfo) {
        val attachedFileInfoList: MutableList<UsedeskFileInfo> = ArrayList(fileInfoListLiveData.getValue())
        attachedFileInfoList.remove(usedeskFileInfo)
        setAttachedFileInfoList(attachedFileInfoList)
    }

    fun onSend(textMessage: String) {
        justComplete(usedeskChat.sendRx(textMessage))
        justComplete(usedeskChat.sendRx(fileInfoListLiveData.value))
        clearFileInfoList()
    }

    fun onSend(name: String, email: String, message: String) {
        justComplete(usedeskChat.sendRx(UsedeskOfflineForm(name, email, message))
                .doOnComplete { messagePanelStateLiveData.postValue(MessagePanelState.OFFLINE_FORM_SENT) })
    }

    fun onNameChanged(name: String) {
        nameLiveData.value = name
    }

    fun onEmailChanged(email: String) {
        emailLiveData.value = email
    }

    init {
        clearFileInfoList()
        toLiveData(actionListenerRx.messagesObservable, messagesLiveData)
        toLiveData(actionListenerRx.offlineFormExpectedObservable
                .map { configuration: UsedeskChatConfiguration ->
                    nameLiveData.postValue(configuration.clientName)
                    emailLiveData.postValue(configuration.email)
                    MessagePanelState.OFFLINE_FORM_EXPECTED
                }, messagePanelStateLiveData)
        toLiveData(actionListenerRx.exceptionObservable, exceptionLiveData)
        disposables.add(actionListenerRx.getConnectedStateSubject()
                .subscribe { connected: Boolean? ->
                    if (!connected!!) {
                        justComplete(usedeskChat.connectRx())
                    }
                })
        feedbacksLiveData.value = HashSet()
    }
}