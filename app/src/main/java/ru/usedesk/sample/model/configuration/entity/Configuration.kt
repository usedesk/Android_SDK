package ru.usedesk.sample.model.configuration.entity

data class Configuration(
        val urlChat: String,
        val urlOfflineForm: String,
        val urlToSendFile: String,
        val urlApi: String,
        val companyId: String,
        val accountId: String,
        val token: String,
        val clientSignature: String,
        val clientEmail: String,
        val clientName: String,
        val clientNote: String,
        val clientPhoneNumber: Long?,
        val clientAdditionalId: Long?,
        val clientInitMessage: String,
        val customAgentName: String,
        val isForegroundService: Boolean,
        val isWithKnowledgeBase: Boolean)