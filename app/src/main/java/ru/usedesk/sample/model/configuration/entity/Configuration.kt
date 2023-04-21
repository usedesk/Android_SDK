
package ru.usedesk.sample.model.configuration.entity

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

data class Configuration(
    val common: Common = Common(),
    val chat: Chat = Chat(),
    val kb: Kb = Kb()
) {
    data class Common(
        val materialComponents: Boolean = false,
        val urlApi: String = "https://secure.usedesk.ru",
        val apiToken: String = "",
        val clientEmail: String = "",
        val clientName: String = ""
    )

    data class Chat(
        val urlChat: String = "https://pubsubsec.usedesk.ru",
        val companyId: String = "",
        val channelId: String = "",
        val clientToken: String = "",
        val clientNote: String = "",
        val clientPhoneNumber: Long? = null,
        val clientAdditionalId: String? = null,
        val clientInitMessage: String = "",
        val clientAvatar: String? = null,
        val customAgentName: String = "",
        val messagesDateFormat: String = "",
        val messageTimeFormat: String = "",
        val messagesPageSize: Int = 20,
        val foregroundService: Boolean = false,
        val cacheFiles: Boolean = true,
        val adaptiveTimePadding: Boolean = true,
        val groupAgentMessages: Boolean = true,
        val additionalFields: Map<Long, String> = mapOf(),
        val additionalNestedFields: List<Map<Long, String>> = listOf()
    )

    data class Kb(
        val withKb: Boolean = true,
        val withKbSupportButton: Boolean = true,
        val noBackStack: Boolean = false,
        val kbId: String = "",
        val sectionId: Long? = null,
        val section: Boolean = false,
        val categoryId: Long? = null,
        val category: Boolean = false,
        val articleId: Long? = null,
        val article: Boolean = false
    )

    fun toChatConfiguration() = UsedeskChatConfiguration(
        urlChat = chat.urlChat,
        urlChatApi = common.urlApi,
        companyId = chat.companyId,
        channelId = chat.channelId,
        messagesPageSize = chat.messagesPageSize,
        clientToken = when (chat.clientToken) {
            "null" -> null
            else -> chat.clientToken
        },
        clientEmail = common.clientEmail,
        clientName = common.clientName,
        clientNote = chat.clientNote,
        clientPhoneNumber = chat.clientPhoneNumber,
        clientAdditionalId = chat.clientAdditionalId,
        clientInitMessage = chat.clientInitMessage,
        clientAvatar = chat.clientAvatar,
        cacheMessagesWithFile = chat.cacheFiles,
        additionalFields = chat.additionalFields,
        additionalNestedFields = chat.additionalNestedFields
    )

    fun toKbConfiguration() = UsedeskKnowledgeBaseConfiguration(
        urlApi = common.urlApi,
        accountId = kb.kbId,
        token = common.apiToken,
        clientEmail = common.clientEmail,
        clientName = common.clientName
    )
}