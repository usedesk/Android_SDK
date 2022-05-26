package ru.usedesk.chat_sdk.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

@Parcelize
data class UsedeskChatConfiguration @JvmOverloads constructor(
    val urlChat: String = "https://pubsubsec.usedesk.ru",
    val urlOfflineForm: String = "https://secure.usedesk.ru/",
    val companyId: String,
    val channelId: String,
    val clientToken: String? = null,
    val clientEmail: String? = null,
    val clientName: String? = null,
    val clientNote: String? = null,
    val clientPhoneNumber: Long? = null,
    val clientAdditionalId: String? = null,
    val clientInitMessage: String? = null,
    val clientAvatar: String? = null,
    val cacheMessagesWithFile: Boolean = true,
    val additionalFields: Map<Long, String> = mapOf(),
    val additionalNestedFields: List<Map<Long, String>> = listOf()
) : Parcelable {
    fun getCompanyAndChannel(): String = "${companyId}_$channelId"

    fun validate(): Validation {
        return Validation(
            validUrlChat = UsedeskValidatorUtil.isValidUrlNecessary(urlChat),
            validUrlOfflineForm = UsedeskValidatorUtil.isValidUrlNecessary(urlOfflineForm),
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
        val validUrlChat: Boolean,
        val validUrlOfflineForm: Boolean,
        val validCompanyId: Boolean,
        val validChannelId: Boolean,
        val validClientToken: Boolean,
        val validClientEmail: Boolean,
        val validClientPhoneNumber: Boolean
    ) {
        fun isAllValid(): Boolean {
            return validUrlChat
                    && validUrlOfflineForm
                    && validCompanyId
                    && validChannelId
                    && validClientEmail
                    && validClientPhoneNumber
        }

        override fun toString(): String {
            return "Validation(validUrlChat=$validUrlChat, " +
                    "validUrlOfflineForm=$validUrlOfflineForm, " +
                    "validCompanyId=$validCompanyId, " +
                    "validChannelId=$validChannelId, " +
                    "validClientToken=$validClientToken, " +
                    "validClientEmail=$validClientEmail, " +
                    "validClientPhoneNumber=$validClientPhoneNumber)"
        }
    }
}