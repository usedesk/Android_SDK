
package ru.usedesk.chat_gui.chat.offlineform

import kotlinx.coroutines.launch
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel.Model.OfflineFormItem
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.domain.IUsedeskChat.SendOfflineFormResult
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

internal class OfflineFormViewModel : UsedeskViewModel<OfflineFormViewModel.Model>(Model()) {

    private val usedeskChat = UsedeskChatSdk.requireInstance()
    private val configuration = UsedeskChatSdk.requireConfiguration()

    private val actionListener: IUsedeskActionListener = object : IUsedeskActionListener {
        override fun onModel(
            model: IUsedeskChat.Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            mainScope.launch {
                setModel {
                    when (model.offlineFormSettings) {
                        offlineFormSettings -> this
                        else -> {
                            val newModel = copy(offlineFormSettings = model.offlineFormSettings)
                            when (val offlineFormSettings = model.offlineFormSettings) {
                                null -> newModel
                                else -> {
                                    val subjectField = OfflineFormItem.List(
                                        TOPIC_KEY,
                                        offlineFormSettings.topicsTitle,
                                        offlineFormSettings.topicsRequired,
                                        offlineFormSettings.topics,
                                        -1
                                    )
                                    val additionalFields =
                                        offlineFormSettings.fields.map { customField ->
                                            OfflineFormItem.Text(
                                                customField.key,
                                                customField.placeholder,
                                                customField.required,
                                                ""
                                            )
                                        }
                                    val customFields = listOf(subjectField) + additionalFields
                                    newModel.copy(
                                        greetings = offlineFormSettings.callbackGreeting,
                                        workType = offlineFormSettings.workType,
                                        customFields = customFields
                                    )
                                }
                            }
                        }
                    }.updateAllFields()
                        .updateSendEnabled()
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
                .updateSendEnabled()
        }
        usedeskChat.addActionListener(actionListener)
    }

    private fun Model.updateAllFields() = copy(
        allFields = listOf(nameField, emailField) + customFields + messageField
    )

    private fun Model.updateSendEnabled() = copy(
        sendEnabled = allFields.all {
            !it.required || when (it) {
                is OfflineFormItem.List -> it.items.getOrNull(it.selected) != null
                is OfflineFormItem.Text -> it.text.isNotEmpty()
            }
        }
    )

    fun sendClicked() {
        val model = setModel {
            val newModel = copy(
                nameField = nameField.copy(
                    error = nameField.required && !nameField.text.any(Char::isLetterOrDigit)
                ),
                emailField = emailField.copy(
                    error = !UsedeskValidatorUtil.isValidEmailNecessary(emailField.text)
                ),
                messageField = messageField.copy(
                    error = messageField.required && !messageField.text.any(Char::isLetterOrDigit)
                ),
                customFields = customFields.map {
                    when (it) {
                        is OfflineFormItem.List -> it
                        is OfflineFormItem.Text -> it.copy(
                            error = it.required && it.text.any(Char::isLetterOrDigit)
                        )
                    }
                }
            ).updateAllFields()
            val isAllValid = newModel.allFields.all {
                when (it) {
                    is OfflineFormItem.List -> true
                    is OfflineFormItem.Text -> !it.error
                }
            }
            when {
                isAllValid -> {
                    val name = nameField.text
                    val email = emailField.text
                    val subjectField = allFields[2] as OfflineFormItem.List
                    val subject = subjectField.items.getOrNull(subjectField.selected) ?: ""
                    val message = (allFields.last() as OfflineFormItem.Text).text
                    val additionalFields = allFields
                        .drop(3)
                        .dropLast(1)
                        .map { field ->
                            UsedeskOfflineForm.Field(
                                field.key,
                                field.title,
                                (field as OfflineFormItem.Text).text
                            )
                        }
                    val offlineForm = UsedeskOfflineForm(
                        name,
                        email,
                        subject,
                        additionalFields,
                        message
                    )
                    newModel.update {
                        copy(
                            offlineFormState = OfflineFormState.SENDING,
                            action = UsedeskEvent {
                                UsedeskChatSdk.requireInstance().send(offlineForm) { result ->
                                    setModel {
                                        when (result) {
                                            SendOfflineFormResult.Done -> copy(
                                                offlineFormState = OfflineFormState.SENT_SUCCESSFULLY,
                                                goExit = UsedeskEvent(
                                                    workType == WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT
                                                )
                                            )
                                            is SendOfflineFormResult.Error -> copy(
                                                offlineFormState = OfflineFormState.FAILED_TO_SEND
                                            )
                                        }
                                    }
                                }
                            },
                            hideKeyboard = UsedeskEvent(Unit)
                        )
                    }
                }
                else -> newModel.update {
                    copy(
                        fieldFocus = UsedeskEvent(allFields.firstOrNull {
                            (it as? OfflineFormItem.Text)?.error == true
                        }?.key)
                    )
                }
            }
        }
        model.action?.use { it() }
    }

    private fun OfflineFormItem.Text.validate(): Boolean = when (key) {
        EMAIL_KEY -> UsedeskValidatorUtil.isValidEmailNecessary(text)
        else -> text.isNotEmpty()
    }

    fun onTextFieldChanged(key: String, text: String) {
        setModel {
            copy(
                nameField = nameField.update(key, text),
                emailField = emailField.update(key, text),
                messageField = messageField.update(key, text),
                customFields = customFields.map {
                    when (it) {
                        is OfflineFormItem.List -> it
                        is OfflineFormItem.Text -> it.update(key, text)
                    }
                }
            ).updateAllFields()
                .updateSendEnabled()
        }
    }

    fun onListFieldChanged(key: String, item: String?) {
        setModel {
            copy(
                customFields = customFields.map {
                    when (it) {
                        is OfflineFormItem.List -> {
                            val index = when (item) {
                                in it.items -> it.items.indexOf(item)
                                else -> -1
                            }
                            it.update(key, index)
                        }
                        is OfflineFormItem.Text -> it
                    }
                }
            ).updateAllFields()
                .updateSendEnabled()
        }
    }

    private fun OfflineFormItem.Text.update(key: String, text: String) = when (key) {
        this.key -> copy(
            text = text,
            error = false
        )
        else -> this
    }

    private fun OfflineFormItem.List.update(key: String, selected: Int) = when (key) {
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
        val nameField: OfflineFormItem.Text = OfflineFormItem.Text(
            key = NAME_KEY,
            required = true
        ),
        val emailField: OfflineFormItem.Text = OfflineFormItem.Text(
            key = EMAIL_KEY,
            required = true
        ),
        val messageField: OfflineFormItem.Text = OfflineFormItem.Text(
            key = MESSAGE_KEY,
            required = true
        ),
        val customFields: List<OfflineFormItem> = listOf(),
        val allFields: List<OfflineFormItem> = listOf(),
        val sendEnabled: Boolean = false,
        val goExit: UsedeskEvent<Boolean>? = null,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val fieldFocus: UsedeskEvent<String?>? = null,
        val hideKeyboard: UsedeskEvent<Unit>? = null,
        val action: UsedeskEvent<() -> Unit>? = null
    ) {
        sealed interface OfflineFormItem {
            val key: String
            val title: String
            val required: Boolean

            data class List(
                override val key: String,
                override val title: String,
                override val required: Boolean,
                val items: kotlin.collections.List<String>,
                val selected: Int
            ) : OfflineFormItem

            data class Text(
                override val key: String,
                override val title: String = "",
                override val required: Boolean = false,
                val text: String = "",
                val error: Boolean = false
            ) : OfflineFormItem
        }
    }

    companion object {
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val MESSAGE_KEY = "message"
        private const val TOPIC_KEY = "topic"
    }
}