package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.repository._extra.Converter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.ChatInited
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import javax.inject.Inject

internal class InitChatResponseConverter @Inject constructor(
    private val messageResponseConverter: MessageResponseConverter
) : Converter<InitChatResponse, ChatInited>() {

    override fun convert(from: InitChatResponse) = ChatInited(
        from.token!!,
        true,
        convert(from.setup?.messages ?: listOf()),
        convert(from.setup?.callbackSettings, from.setup!!.noOperators),
        from.setup?.ticket?.statusId
    )

    private fun convert(
        callbackSettings: InitChatResponse.Setup.CallbackSettings?,
        noOperators: Boolean?
    ): UsedeskOfflineFormSettings {
        return convertOrNull {
            val topics = callbackSettings!!.topics
                ?.filter { it?.checked == true }
                ?.mapNotNull { it?.text }
                ?: listOf()
            val workType = UsedeskOfflineFormSettings.WorkType.values()
                .firstOrNull {
                    it.name.equals(callbackSettings.workType ?: "", ignoreCase = true)
                }
                ?: UsedeskOfflineFormSettings.WorkType.NEVER
            val customFields = callbackSettings.customFields
                ?.mapIndexed { index, customField ->
                    convertOrNull {
                        val required = customField!!.required == true
                        UsedeskOfflineFormSettings.CustomField(
                            "custom_field_$index",
                            required,
                            customField.checked ?: false,
                            customField.placeholder ?: ""
                        )
                    }
                }
                ?.filterNotNull()
                ?.filter(UsedeskOfflineFormSettings.CustomField::checked)
                ?: listOf()
            val required = callbackSettings.topicsRequired == 1
            UsedeskOfflineFormSettings(
                noOperators == true,
                workType,
                callbackSettings.callbackTitle ?: "",
                callbackSettings.callbackGreeting ?: "",
                customFields,
                topics,
                callbackSettings.topicsTitle ?: "",
                required
            )
        } ?: UsedeskOfflineFormSettings(
            noOperators == true,
            UsedeskOfflineFormSettings.WorkType.NEVER
        )
    }

    private fun convert(messages: List<MessageResponse.Message?>) = messages.flatMap {
        convertOrNull { messageResponseConverter.convert(it) } ?: listOf()
    }
}