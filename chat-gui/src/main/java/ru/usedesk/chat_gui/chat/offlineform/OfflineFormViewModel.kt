package ru.usedesk.chat_gui.chat.offlineform

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

class OfflineFormViewModel : UsedeskViewModel() {

    val nameLiveData = MutableLiveData("")
    val emailLiveData = MutableLiveData("")
    val subjectLiveData = MutableLiveData<Int>(-1)
    val additionalsLiveData = MutableLiveData(mapOf<Int, String>())
    val messageLiveData = MutableLiveData("")

    val offlineFormStateLiveData = MutableLiveData(OfflineFormState.DEFAULT)
    val nameErrorLiveData = MutableLiveData(false)
    val emailErrorLiveData = MutableLiveData(false)
    val messageErrorLiveData = MutableLiveData(false)
    val offlineFormSettingsLiveData = MutableLiveData<UsedeskOfflineFormSettings>()

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()
    private val actionListenerRx: IUsedeskActionListenerRx

    val configuration = UsedeskChatSdk.requireConfiguration()

    init {
        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onOfflineFormExpectedObservable(
                    offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
            ): Disposable? {
                return offlineFormExpectedObservable.subscribe {
                    offlineFormSettingsLiveData.postValue(it)
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
    }

    fun onSendOfflineForm() {
        val name = nameLiveData.value ?: ""
        val email = emailLiveData.value ?: ""
        val message = messageLiveData.value ?: ""

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

    fun onOfflineFormNameChanged(name: String) {
        nameLiveData.value = name
        nameErrorLiveData.value = false
    }

    fun onOfflineFormEmailChanged(email: String) {
        emailLiveData.value = email
        emailErrorLiveData.value = false
    }

    fun onOfflineFormSubjectChanged(index: Int) {
        subjectLiveData.value = index
    }

    fun onOfflineFormAdditionalChanged(index: Int, text: String) {
        val newMap = additionalsLiveData.value?.toMutableMap() ?: mutableMapOf()
        newMap[index] = text
        additionalsLiveData.value = newMap
    }

    fun onOfflineFormMessageChanged(message: String) {
        messageLiveData.value = message
        messageErrorLiveData.value = false
    }

    override fun onCleared() {
        super.onCleared()

        UsedeskChatSdk.getInstance()
                ?.removeActionListener(actionListenerRx)
    }

    enum class OfflineFormState {
        DEFAULT,
        SENDING,
        SENT_SUCCESSFULLY,
        FAILED_TO_SEND
    }

}