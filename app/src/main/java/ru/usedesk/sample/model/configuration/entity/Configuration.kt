
package ru.usedesk.sample.model.configuration.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

data class Configuration(
    @SerializedName("common") val common: Common = Common(),
    @SerializedName("chat") val chat: Chat = Chat(),
    @SerializedName("kb") val kb: Kb = Kb()
) {
    data class Common(
        @SerializedName("materialComponents") val materialComponents: Boolean = false,
        @SerializedName("urlApi") val urlApi: String = "https://secure.usedesk.ru",
        @SerializedName("apiToken") val apiToken: String = "",
        @SerializedName("clientEmail") val clientEmail: String = "",
        @SerializedName("clientName") val clientName: String = ""
    )

    data class Chat(
        @SerializedName("urlChat") val urlChat: String = "https://pubsubsec.usedesk.ru",
        @SerializedName("companyId") val companyId: String = "",
        @SerializedName("channelId") val channelId: String = "",
        @SerializedName("clientId") val clientId: String = "",
        @SerializedName("clientToken") val clientToken: String = "",
        @SerializedName("clientNote") val clientNote: String = "",
        @SerializedName("clientPhoneNumber") val clientPhoneNumber: Long? = null,
        @SerializedName("clientAdditionalId") val clientAdditionalId: String? = null,
        @SerializedName("clientInitMessage") val clientInitMessage: String = "",
        @SerializedName("clientAvatar") val clientAvatar: String? = null,
        @SerializedName("customAgentName") val customAgentName: String = "",
        @SerializedName("messagesDateFormat") val messagesDateFormat: String = "",
        @SerializedName("messageTimeFormat") val messageTimeFormat: String = "",
        @SerializedName("messagesPageSize") val messagesPageSize: Int = 20,
        @SerializedName("foregroundService") val foregroundService: Boolean = false,
        @SerializedName("cacheFiles") val cacheFiles: Boolean = true,
        @SerializedName("adaptiveTimePadding") val adaptiveTimePadding: Boolean = true,
        @SerializedName("customRating") val customRating: Boolean = false,
        @SerializedName("groupAgentMessages") val groupAgentMessages: Boolean = true,
        @SerializedName("additionalFields") val additionalFields: Map<Long, String> = mapOf(),
        @SerializedName("additionalNestedFields") val additionalNestedFields: List<Map<Long, String>> = listOf()
    )

    data class Kb(
        @SerializedName("withKb") val withKb: Boolean = true,
        @SerializedName("withKbSupportButton") val withKbSupportButton: Boolean = true,
        @SerializedName("noBackStack") val noBackStack: Boolean = false,
        @SerializedName("kbId") val kbId: String = "",
        @SerializedName("sectionId") val sectionId: Long? = null,
        @SerializedName("section") val section: Boolean = false,
        @SerializedName("categoryId") val categoryId: Long? = null,
        @SerializedName("category") val category: Boolean = false,
        @SerializedName("articleId") val articleId: Long? = null,
        @SerializedName("article") val article: Boolean = false
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
        additionalNestedFields = chat.additionalNestedFields,
        clientId = chat.clientId,
    )

    fun toKbConfiguration() = UsedeskKnowledgeBaseConfiguration(
        urlApi = common.urlApi,
        accountId = kb.kbId,
        token = common.apiToken,
        clientEmail = common.clientEmail,
        clientName = common.clientName
    )
}
