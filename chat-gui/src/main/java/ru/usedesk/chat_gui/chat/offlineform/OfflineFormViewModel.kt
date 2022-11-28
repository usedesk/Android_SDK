package ru.usedesk.chat_gui.chat.offlineform

import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormItem
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormList
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormText
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat.SendOfflineFormResult
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

internal class OfflineFormViewModel : UsedeskViewModel<OfflineFormViewModel.Model>(Model()) {

    private val usedeskChat = UsedeskChatSdk.requireInstance()
    private val configuration = UsedeskChatSdk.requireConfiguration()

    private val actionListener: IUsedeskActionListener = object : IUsedeskActionListener {
        override fun onOfflineFormExpected(offlineFormSettings: UsedeskOfflineFormSettings) {
            doMain {
                setModel {
                    val subjectField = OfflineFormList(
                        TOPIC_KEY,
                        offlineFormSettings.topicsTitle,
                        offlineFormSettings.topicsRequired,
                        offlineFormSettings.topics,
                        -1
                    )
                    val additionalFields = offlineFormSettings.fields.map { customField ->
                        OfflineFormText(
                            customField.key,
                            customField.placeholder,
                            customField.required,
                            ""
                        )
                    }
                    val customFields = listOf(subjectField) + additionalFields
                    copy(
                        greetings = offlineFormSettings.callbackGreeting,
                        workType = offlineFormSettings.workType,
                        customFields = customFields
                    ).updateAllFields()
                }
            }
        }
    }

    init {
        setModel {
            copy(
                nameField = nameField.copy(text = configuration.clientName ?: ""),
                emailField = emailField.copy(text = configuration.clientEmail ?: "")
            ).updateAllFields()
        }
        usedeskChat.addActionListener(actionListener)
    }

    fun init(
        nameTitle: String,
        emailTitle: String,
        messageTitle: String
    ) {
        setModel {
            copy(
                nameField = nameField.copy(title = nameTitle),
                emailField = emailField.copy(title = emailTitle),
                messageField = messageField.copy(title = messageTitle)
            ).updateAllFields()
        }
    }

    private fun Model.updateAllFields(): Model {
        val allFields = listOf(nameField, emailField) + customFields + messageField
        return copy(
            allFields = allFields,
            sendEnabled = isFieldsValid(allFields)
        )
    }

    fun onSendOfflineForm() {
        setModel {
            val name = (allFields[0] as OfflineFormText).text
            val email = (allFields[1] as OfflineFormText).text
            val subjectField = allFields[2] as OfflineFormList
            val subject = subjectField.items.getOrNull(subjectField.selected) ?: ""
            val message = (allFields.last() as OfflineFormText).text
            val additionalFields = allFields.drop(3).dropLast(1).map { field ->
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
            copy(offlineFormState = OfflineFormState.SENDING).apply {
                UsedeskChatSdk.requireInstance().send(offlineForm) { result ->
                    setModel {
                        when (result) {
                            SendOfflineFormResult.Done -> copy(
                                offlineFormState = OfflineFormState.SENT_SUCCESSFULLY,
                                goExit = UsedeskSingleLifeEvent(
                                    workType == WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT
                                )
                            )
                            is SendOfflineFormResult.Error -> copy(
                                offlineFormState = OfflineFormState.FAILED_TO_SEND
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isFieldsValid(items: List<OfflineFormItem>): Boolean = items.all {
        !it.required || when (it) {
            is OfflineFormText -> when (it.key) {
                EMAIL_KEY -> UsedeskValidatorUtil.isValidEmailNecessary(it.text)
                else -> it.text.isNotEmpty()
            }
            is OfflineFormList -> it.selected >= 0
        }
    }

    fun onTextFieldChanged(key: String, text: String) {
        setModel {
            copy(
                nameField = nameField.update(key, text),
                emailField = emailField.update(key, text),
                messageField = messageField.update(key, text),
                customFields = customFields.map {
                    when (it) {
                        is OfflineFormList -> it
                        is OfflineFormText -> it.update(key, text)
                    }
                }
            ).updateAllFields()
        }
    }

    fun onListFieldChanged(key: String, item: String?) {
        setModel {
            copy(
                customFields = customFields.map {
                    when (it) {
                        is OfflineFormList -> {
                            val index = when (item) {
                                in it.items -> it.items.indexOf(item)
                                else -> -1
                            }
                            it.update(key, index)
                        }
                        is OfflineFormText -> it
                    }
                }
            ).updateAllFields()
        }
    }

    private fun OfflineFormText.update(key: String, text: String) = when (key) {
        this.key -> copy(text = text)
        else -> this
    }

    private fun OfflineFormList.update(key: String, selected: Int) = when (key) {
        this.key -> copy(selected = selected)
        else -> this
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)

        UsedeskChatSdk.release(false)
    }

    enum class OfflineFormState {
        DEFAULT,
        SENDING,
        SENT_SUCCESSFULLY,
        FAILED_TO_SEND
    }

    data class Model(
        val offlineFormState: OfflineFormState = OfflineFormState.DEFAULT,
        val workType: WorkType = WorkType.ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT,
        val greetings: String = "",
        val nameField: OfflineFormText = OfflineFormText(
            key = NAME_KEY,
            required = true
        ),
        val emailField: OfflineFormText = OfflineFormText(
            key = EMAIL_KEY,
            required = true
        ),
        val messageField: OfflineFormText = OfflineFormText(
            key = MESSAGE_KEY,
            required = true
        ),
        val customFields: List<OfflineFormItem> = listOf(),
        val allFields: List<OfflineFormItem> = listOf(),
        val sendEnabled: Boolean = false,
        val goExit: UsedeskSingleLifeEvent<Boolean>? = null
    )

    companion object {
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val MESSAGE_KEY = "message"
        private const val TOPIC_KEY = "topic"
    }
}