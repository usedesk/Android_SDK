package ru.usedesk.chat_gui.chat.offlineform

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

internal class OfflineFormViewModel : UsedeskViewModel<OfflineFormViewModel.Model>(Model()) {

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()
    private var actionListenerRx: IUsedeskActionListenerRx? = null

    fun init(
        nameTitle: String,
        emailTitle: String,
        messageTitle: String
    ) {
        doInit {
            object : IUsedeskActionListenerRx() {
                override fun onOfflineFormExpectedObservable(
                    offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
                ): Disposable? {
                    return offlineFormExpectedObservable.subscribe {
                        val subjectField = OfflineFormList(
                            "topic",
                            it.topicsTitle,
                            it.topicsRequired,
                            it.topics,
                            -1
                        )
                        val additionalFields = it.fields.map { customField ->
                            OfflineFormText(
                                customField.key,
                                customField.placeholder,
                                customField.required,
                                ""
                            )
                        }
                        val configuration = UsedeskChatSdk.requireConfiguration()
                        val nameField = OfflineFormText(
                            "name",
                            nameTitle,
                            true,
                            configuration.clientName ?: ""
                        )
                        val emailField = OfflineFormText(
                            "email",
                            emailTitle,
                            true,
                            configuration.clientEmail ?: ""
                        )
                        val messageField = OfflineFormText(
                            "message",
                            messageTitle,
                            true,
                            ""
                        )
                        val fields = listOf(nameField, emailField, subjectField) +
                                additionalFields +
                                messageField

                        setModel { model ->
                            model.copy(
                                offlineFormSettings = it,
                                fields = fields
                            )
                        }
                    }
                }
            }.let {
                actionListenerRx = it
                usedeskChat.addActionListener(it)
            }
        }
    }

    fun onSendOfflineForm(onSuccess: (Boolean) -> Unit) {
        val model = modelLiveData.value

        if (isFieldsValid(model.fields)) {
            setModel { model ->
                model.copy(
                    offlineFormState = OfflineFormState.SENDING
                )
            }
            val name = (model.fields[0] as OfflineFormText).text
            val email = (model.fields[1] as OfflineFormText).text
            val subjectField = model.fields[2] as OfflineFormList
            val subject = subjectField.items.getOrNull(subjectField.selected) ?: ""
            val message = (model.fields.last() as OfflineFormText).text
            val additionalFields = model.fields.drop(3).dropLast(1).map { field ->
                UsedeskOfflineForm.Field(
                    field.key,
                    field.title,
                    (field as OfflineFormText).text
                )
            }
            val offlineForm = UsedeskOfflineForm(
                name,
                email,
                subject,
                additionalFields,
                message
            )
            doIt(UsedeskChatSdk.requireInstance().sendRx(offlineForm), {
                setModel { model ->
                    model.copy(
                        offlineFormState = OfflineFormState.SENT_SUCCESSFULLY
                    )
                }
                onSuccess(model.offlineFormSettings?.workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT)
            }) {
                setModel { model ->
                    model.copy(
                        offlineFormState = OfflineFormState.FAILED_TO_SEND
                    )
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
            usedeskChat.removeActionListener(it)

            UsedeskChatSdk.release(false)
        }
    }

    fun onTextFieldChanged(index: Int, text: String) {
        val model = modelLiveData.value
        val newFields = model.fields.toMutableList()
        val oldField = newFields[index]
        newFields[index] = OfflineFormText(oldField.key, oldField.title, oldField.required, text)
        setModel { model ->
            model.copy(
                fields = newFields,
                sendEnabled = isFieldsValid(newFields)
            )
        }
    }

    fun setSubject(subject: String) {
        setModel { model ->
            val newFields = model.fields.toMutableList()
            val oldField = newFields[2] as OfflineFormList
            newFields[2] = OfflineFormList(
                oldField.key,
                oldField.title,
                oldField.required,
                oldField.items,
                oldField.items.indexOf(subject)
            )
            model.copy(
                selectedSubject = subject,
                fields = newFields,
                sendEnabled = isFieldsValid(newFields)
            )
        }
    }

    enum class OfflineFormState {
        DEFAULT,
        SENDING,
        SENT_SUCCESSFULLY,
        FAILED_TO_SEND
    }

    data class Model(
        val offlineFormState: OfflineFormState = OfflineFormState.DEFAULT,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val selectedSubject: String = "",
        val fields: List<OfflineFormItem> = listOf(),
        val sendEnabled: Boolean = false
    )
}