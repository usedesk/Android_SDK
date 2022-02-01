package ru.usedesk.sample.model.configuration.entity

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

data class Configuration(
    val urlChat: String = "https://pubsubsec.usedesk.ru",
    val urlOfflineForm: String = "https://secure.usedesk.ru/",
    val urlToSendFile: String = "https://secure.usedesk.ru/uapi/v1/",
    val urlApi: String = "https://api.usedesk.ru/",
    val companyId: String = "153712",
    val channelId: String = "6202",
    val accountId: String = "4",
    val token: String = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75",
    val clientToken: String = "",
    val clientEmail: String = "",
    val clientName: String = "",
    val clientNote: String = "",
    val clientPhoneNumber: Long? = null,
    val clientAdditionalId: String? = null,
    val clientInitMessage: String = "",
    val clientAvatar: String? = null,
    val customAgentName: String = "",
    val foregroundService: Boolean? = null,
    val cacheFiles: Boolean = true,
    val additionalFields: Map<Long, String> = mapOf(),
    val additionalNestedFields: List<Map<Long, String>> = listOf(),
    val withKb: Boolean = true,
    val withKbSupportButton: Boolean = true,
    val withKbArticleRating: Boolean = true
) {

    fun toChatConfiguration(): UsedeskChatConfiguration {
        val defaultChatConfiguration = UsedeskChatConfiguration(
            urlChat = urlChat,
            companyId = companyId,
            channelId = channelId
        )
        return UsedeskChatConfiguration(
            urlChat,
            urlOfflineForm.ifEmpty { defaultChatConfiguration.urlOfflineForm },
            urlToSendFile.ifEmpty { defaultChatConfiguration.urlToSendFile },
            companyId,
            channelId,
            clientToken,
            clientEmail,
            clientName,
            clientNote,
            clientPhoneNumber,
            clientAdditionalId,
            clientInitMessage,
            cacheFiles,
            additionalFields,
            additionalNestedFields
        )
    }

    fun toKbConfiguration(): UsedeskKnowledgeBaseConfiguration {
        val defaultConfiguration = UsedeskKnowledgeBaseConfiguration(
            accountId,
            token,
            clientEmail
        )
        return UsedeskKnowledgeBaseConfiguration(
            urlApi.ifEmpty { defaultConfiguration.urlApi },
            accountId,
            token,
            clientEmail,
            clientName
        )
    }
}