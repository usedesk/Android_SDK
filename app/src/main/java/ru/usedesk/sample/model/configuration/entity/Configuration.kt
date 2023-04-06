package ru.usedesk.sample.model.configuration.entity

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

data class Configuration(
    val materialComponents: Boolean = false,
    val urlChat: String = "https://pubsubsec.usedesk.ru",
    val urlChatApi: String = "https://secure.usedesk.ru",
    val urlApi: String = "https://api.usedesk.ru",
    val companyId: String = "",
    val channelId: String = "",
    val accountId: String = "",
    val messagesPageSize: Int = 20,
    val token: String = "",
    val clientToken: String = "",
    val clientEmail: String = "",
    val clientName: String = "",
    val clientNote: String = "",
    val clientPhoneNumber: Long? = null,
    val clientAdditionalId: String? = null,
    val clientInitMessage: String = "",
    val clientAvatar: String? = null,
    val customAgentName: String = "",
    val messagesDateFormat: String = "",
    val messageTimeFormat: String = "",
    val foregroundService: Boolean = false,
    val cacheFiles: Boolean = true,
    val groupAgentMessages: Boolean = true,
    val adaptiveTimePadding: Boolean = true,
    val additionalFields: Map<Long, String> = mapOf(),
    val additionalNestedFields: List<Map<Long, String>> = listOf(),
    val withKb: Boolean = true,
    val withKbSupportButton: Boolean = true
) {

    fun toChatConfiguration() = UsedeskChatConfiguration(
        urlChat = urlChat,
        urlChatApi = urlChatApi,
        companyId = companyId,
        channelId = channelId,
        messagesPageSize = messagesPageSize,
        clientToken = when (clientToken) {
            "null" -> null
            else -> clientToken
        },
        clientEmail = clientEmail,
        clientName = clientName,
        clientNote = clientNote,
        clientPhoneNumber = clientPhoneNumber,
        clientAdditionalId = clientAdditionalId,
        clientInitMessage = clientInitMessage,
        clientAvatar = clientAvatar,
        cacheMessagesWithFile = cacheFiles,
        additionalFields = additionalFields,
        additionalNestedFields = additionalNestedFields
    )

    fun toKbConfiguration() = UsedeskKnowledgeBaseConfiguration(
        urlApi = urlApi,
        accountId = accountId,
        token = token,
        clientEmail = clientEmail,
        clientName = clientName
    )
}