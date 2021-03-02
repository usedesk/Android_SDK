package ru.usedesk.sample.model.configuration.entity

data class Configuration(
        val urlChat: String = "https://pubsub.usedesk.ru:1992",
        val urlOfflineForm: String = "https://secure.usedesk.ru/",
        val urlToSendFile: String = "https://secure.usedesk.ru/uapi/v1/",
        val urlApi: String = "https://api.usedesk.ru/",
        val companyId: String = "153712",
        val accountId: String = "4",
        val token: String = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75",
        val clientSignature: String = "",
        val clientEmail: String = "android_sdk@usedesk.ru",
        val clientName: String = "Иван Иванов",
        val clientNote: String = "",
        val clientPhoneNumber: Long? = 88005553535,
        val clientAdditionalId: Long? = 777,
        val clientInitMessage: String = "",
        val customAgentName: String = "",
        val foregroundService: Boolean = false,
        val withKnowledgeBase: Boolean = true,
        val withSupportButton: Boolean = true)