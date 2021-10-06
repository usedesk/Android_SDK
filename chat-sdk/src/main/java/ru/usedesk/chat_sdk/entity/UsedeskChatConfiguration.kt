package ru.usedesk.chat_sdk.entity

import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

data class UsedeskChatConfiguration @JvmOverloads constructor(
    val urlChat: String,
    val urlOfflineForm: String = "https://secure.usedesk.ru/",
    val urlToSendFile: String = "https://secure.usedesk.ru/uapi/v1/",
    val companyId: String,
    val channelId: String,
    val clientToken: String? = null,
    val clientEmail: String? = null,
    val clientName: String? = null,
    val clientNote: String? = null,
    val clientPhoneNumber: Long? = null,
    val clientAdditionalId: Long? = null,
    val clientInitMessage: String? = null,
    val cacheMessagesWithFile: Boolean = true,
    val additionalFields: Map<Long, String> = mapOf(),
    val additionalNestedFields: List<Map<Long, String>> = listOf()
) {
    fun getCompanyAndChannel(): String = "${companyId}_$channelId"

    fun validate(): Validation {
        return Validation(
            validUrlChat = UsedeskValidatorUtil.isValidUrlNecessary(urlChat),
            validUrlOfflineForm = UsedeskValidatorUtil.isValidUrlNecessary(urlOfflineForm),
            validUrlToSendFile = UsedeskValidatorUtil.isValidUrlNecessary(urlToSendFile),
            validCompanyId = isNotEmptyNumber(companyId),
            validChannelId = isNotEmptyNumber(channelId),
            validClientToken = isValidClientToken(clientToken),
            validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail),
            validClientPhoneNumber = UsedeskValidatorUtil.isValidPhone(clientPhoneNumber)
        )
    }

    private fun isNotEmptyNumber(value: String): Boolean {
        return value.isNotEmpty() && value.all { it in '0'..'9' }
    }

    private fun isValidClientToken(value: String?): Boolean {
        return value == null || value.length >= 64
    }

    class Validation(
        val validUrlChat: Boolean = false,
        val validUrlOfflineForm: Boolean = false,
        val validUrlToSendFile: Boolean = false,
        val validCompanyId: Boolean = false,
        val validChannelId: Boolean = false,
        val validClientToken: Boolean = false,
        val validClientEmail: Boolean = false,
        val validClientPhoneNumber: Boolean = false
    ) {
        fun isAllValid(): Boolean {
            return validUrlChat
                    && validUrlOfflineForm
                    && validUrlToSendFile
                    && validCompanyId
                    && validChannelId
                    && validClientEmail
                    && validClientPhoneNumber
        }

        override fun toString(): String {
            return "Validation(validUrlChat=$validUrlChat, " +
                    "validUrlOfflineForm=$validUrlOfflineForm, " +
                    "validUrlToSendFile=$validUrlToSendFile, " +
                    "validCompanyId=$validCompanyId, " +
                    "validChannelId=$validChannelId, " +
                    "validClientToken=$validClientToken, " +
                    "validClientEmail=$validClientEmail, " +
                    "validClientPhoneNumber=$validClientPhoneNumber)"
        }
    }
}