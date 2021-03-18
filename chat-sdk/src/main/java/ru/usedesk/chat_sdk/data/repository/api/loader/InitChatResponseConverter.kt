package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.Converter
import ru.usedesk.chat_sdk.data.repository.api.entity.ChatInited
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import toothpick.InjectConstructor

@InjectConstructor
internal class InitChatResponseConverter(
        private val messageResponseConverter: MessageResponseConverter
) : Converter<InitChatResponse, ChatInited>() {

    override fun convert(from: InitChatResponse): ChatInited {
        return ChatInited(
                from.token!!,
                true,
                convert(from.setup?.messages ?: listOf()),
                convert(from.setup?.callbackSettings, from.setup!!.noOperators)
        )
    }

    private fun convert(callbackSettings: InitChatResponse.Setup.CallbackSettings?,
                        noOperators: Boolean?): UsedeskOfflineFormSettings {
        return convertOrNull {
            val topics = callbackSettings!!.topics?.filter {
                it?.checked == true
            }?.mapNotNull {
                it?.text
            } ?: listOf()
            val workType = UsedeskOfflineFormSettings.WorkType.values().firstOrNull {
                it.name.equals(callbackSettings.workType ?: "", ignoreCase = true)
            } ?: UsedeskOfflineFormSettings.WorkType.NEVER
            val customFields = callbackSettings.customFields?.mapNotNull { customField ->
                convertOrNull {
                    val required = customField!!.required == true
                    val type = UsedeskOfflineFormSettings.CustomField.Type.values().first {
                        customField.type.equals(it.name, ignoreCase = true)
                    }
                    UsedeskOfflineFormSettings.CustomField(
                            type,
                            required,
                            customField.placeholder ?: ""
                    )
                }
            } ?: listOf()
            val required = callbackSettings.topicsRequired == 1
            UsedeskOfflineFormSettings(
                    noOperators == true,
                    UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT,//TODO:DEBUG //workType,
                    callbackSettings.callbackTitle,
                    callbackSettings.callbackGreeting,
                    customFields,
                    topics,
                    callbackSettings.topicsTitle,
                    required
            )
        } ?: UsedeskOfflineFormSettings(
                noOperators == true,
                UsedeskOfflineFormSettings.WorkType.NEVER
        )
    }

    private fun convert(messages: List<MessageResponse.Message?>): List<UsedeskMessage> {
        return messages.flatMap {
            convertOrNull {
                messageResponseConverter.convert(it)
            } ?: listOf()
        }
    }
}