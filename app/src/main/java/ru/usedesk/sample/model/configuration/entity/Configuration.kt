package ru.usedesk.sample.model.configuration.entity

data class Configuration(
    val urlChat: String,
    val urlOfflineForm: String,
    val urlToSendFile: String,
    val urlApi: String,
    val companyId: String,
    val channelId: String,
    val accountId: String,
    val token: String,
    val clientToken: String,
    val clientEmail: String,
    val clientName: String,
    val clientNote: String,
    val clientPhoneNumber: Long?,
    val clientAdditionalId: Long?,
    val clientInitMessage: String,
    val customAgentName: String,
    val foregroundService: Boolean,
    val cacheFiles: Boolean,
    val additionalFields: Map<Long, String>,
    val additionalNestedFields: List<Map<Long, String>>,
    val withKb: Boolean,
    val withKbSupportButton: Boolean,
    val withKbArticleRating: Boolean
) {
    companion object {
        val default = Configuration(
            urlChat = "https://pubsubsec.usedesk.ru",
            urlOfflineForm = "https://secure.usedesk.ru/",
            urlToSendFile = "https://secure.usedesk.ru/uapi/v1/",
            urlApi = "https://api.usedesk.ru/",
            companyId = "153712",
            channelId = "6202",
            accountId = "4",
            token = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75",
            clientToken = "",
            clientEmail = "android_sdk@usedesk.ru",
            clientName = "Иван Иванов",
            clientNote = "",
            clientPhoneNumber = 88005553535,
            clientAdditionalId = 777,
            clientInitMessage = "",
            customAgentName = "",
            foregroundService = false,
            cacheFiles = true,
            additionalFields = mapOf(),
            additionalNestedFields = listOf(),
            withKb = true,
            withKbSupportButton = true,
            withKbArticleRating = true
        )
    }
}