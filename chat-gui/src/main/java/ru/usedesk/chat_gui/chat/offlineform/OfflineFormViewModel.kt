package ru.usedesk.chat_gui.chat.offlineform

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormItem
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormList
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormText
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

internal class OfflineFormViewModel : UsedeskViewModel() {

    val offlineFormStateLiveData = MutableLiveData<OfflineFormState?>(OfflineFormState.DEFAULT)
    val offlineFormSettings = MutableLiveData<UsedeskOfflineFormSettings?>()
    val fieldsLiveData = MutableLiveData<List<OfflineFormItem>?>()
    val sendEnabledLiveData = MutableLiveData<Boolean?>(false)

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()
    private var actionListenerRx: IUsedeskActionListenerRx? = null

    val configuration = UsedeskChatSdk.requireConfiguration()

    fun init(nameTitle: String,
             emailTitle: String,
             messageTitle: String
    ) {
        doInit {
            object : IUsedeskActionListenerRx() {
                override fun onOfflineFormExpectedObservable(
                        offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
                ): Disposable? {
                    return offlineFormExpectedObservable.subscribe {
                        offlineFormSettings.postValue(it)
                        val subjectField = OfflineFormList("topic",
                                it.topicsTitle,
                                it.topicsRequired,
                                it.topics,
                                -1
                        )
                        val additionalFields = it.fields.map { customField ->
                            OfflineFormText(customField.key,
                                    customField.placeholder,
                                    customField.required,
                                    "")
                        }
                        val configuration = UsedeskChatSdk.requireConfiguration()
                        val nameField = OfflineFormText("name",
                                nameTitle,
                                true,
                                configuration.clientName ?: "")
                        val emailField = OfflineFormText("email",
                                emailTitle,
                                true,
                                configuration.clientEmail ?: "")
                        val messageField = OfflineFormText("message",
                                messageTitle,
                                true,
                                "")
                        val fields = listOf(nameField, emailField, subjectField) +
                                additionalFields +
                                messageField
                        fieldsLiveData.postValue(fields)
                    }
                }
            }.let {
                actionListenerRx = it
                usedeskChat.addActionListener(it)
            }
        }
    }

    fun onSendOfflineForm(onSuccess: (Boolean) -> Unit) {
        fieldsLiveData.value?.let { it ->
            if (isFieldsValid(it)) {
                offlineFormStateLiveData.postValue(OfflineFormState.SENDING)
                val name = (it[0] as OfflineFormText).text
                val email = (it[1] as OfflineFormText).text
                val subjectField = it[2] as OfflineFormList
                val subject = subjectField.items.getOrNull(subjectField.selected) ?: ""
                val message = (it.last() as OfflineFormText).text
                val additionalFields = it.drop(3).dropLast(1).map { field ->
                    UsedeskOfflineForm.Field(field.key, field.title, (field as OfflineFormText).text)
                }
                val offlineForm = UsedeskOfflineForm(
                        name,
                        email,
                        subject,
                        additionalFields,
                        message
                )
                doIt(UsedeskChatSdk.requireInstance().sendRx(offlineForm), {
                    offlineFormStateLiveData.postValue(OfflineFormState.SENT_SUCCESSFULLY)
                    onSuccess(offlineFormSettings.value?.workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT)
                }) {
                    offlineFormStateLiveData.postValue(OfflineFormState.FAILED_TO_SEND)
                }
            }
        }
    }

    private fun isFieldsValid(items: List<OfflineFormItem>): Boolean {
        val email = items[1] as OfflineFormText
        val emailIsValid = UsedeskValidatorUtil.isValidEmailNecessary(email.text)
        return emailIsValid && items.all {
            !it.required || when (it) {
                is OfflineFormText -> {
                    it.text.isNotEmpty()
                }
                is OfflineFormList -> {
                    it.selected >= 0
                }
                else -> true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        actionListenerRx?.let {
            UsedeskChatSdk.getInstance()
                    ?.removeActionListener(it)
        }
    }

    fun onTextFieldChanged(index: Int, text: String) {
        val newFields = fieldsLiveData.value!!.toMutableList()
        val oldField = newFields[index]
        newFields[index] = OfflineFormText(oldField.key, oldField.title, oldField.required, text)
        fieldsLiveData.value = newFields
        sendEnabledLiveData.value = isFieldsValid(newFields)
    }

    fun onSubjectIndexChanged(index: Int) {
        val newFields = fieldsLiveData.value!!.toMutableList()
        val oldField = newFields[2] as OfflineFormList
        newFields[2] = OfflineFormList(oldField.key, oldField.title, oldField.required, oldField.items, index)
        fieldsLiveData.value = newFields
        sendEnabledLiveData.value = isFieldsValid(newFields)
    }

    enum class OfflineFormState {
        DEFAULT,
        SENDING,
        SENT_SUCCESSFULLY,
        FAILED_TO_SEND
    }
}