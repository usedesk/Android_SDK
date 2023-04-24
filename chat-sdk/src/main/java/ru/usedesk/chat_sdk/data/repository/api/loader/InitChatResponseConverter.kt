
package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.chat_sdk.entity.ChatInited
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_sdk.api.UsedeskApiRepository.Companion.valueOrNull
import javax.inject.Inject

internal class InitChatResponseConverter @Inject constructor(
    private val messageResponseConverter: IMessageResponseConverter
) : IInitChatResponseConverter {

    override fun convert(from: SocketResponse.Inited): ChatInited {
        val messages = convert(from.setup?.messages ?: listOf()).flatten()
        return ChatInited(
            from.token!!,
            true,
            messages,
            convert(from.setup?.callbackSettings, from.setup!!.noOperators),
            from.setup.ticket?.statusId
        )
    }

    private fun convert(
        callbackSettings: SocketResponse.Inited.Setup.CallbackSettings?,
        noOperators: Boolean?
    ): UsedeskOfflineFormSettings = valueOrNull {
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
                valueOrNull {
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

    private fun convert(messages: List<SocketResponse.AddMessage.Message?>) =
        messages.map(messageResponseConverter::convert)
}