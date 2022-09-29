package ru.usedesk.chat_sdk.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

@Parcelize
data class UsedeskChatConfiguration @JvmOverloads constructor(
    val urlChat: String = "https://pubsubsec.usedesk.ru",
    val urlChatApi: String = "https://secure.usedesk.ru/",
    val companyId: String,
    val channelId: String,
    val messagesPageSize: Int = 20,
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
    @IgnoredOnParcel
    val userKey: String = """${clientEmail ?: ""}_${clientPhoneNumber ?: ""}_${clientName ?: ""}"""

    fun getCompanyAndChannel(): String = "${companyId}_$channelId"

    fun validate(): Validation {
        return Validation(
            validUrlChat = UsedeskValidatorUtil.isValidUrlNecessary(urlChat),
            validUrlOfflineForm = UsedeskValidatorUtil.isValidUrlNecessary(urlChatApi),
            validCompanyId = isNotEmptyNumber(companyId),
            validChannelId = isNotEmptyNumber(channelId),
            validClientToken = isValidClientToken(clientToken),
            validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail),
            validClientPhoneNumber = UsedeskValidatorUtil.isValidPhone(clientPhoneNumber)
        )
    }

    private fun isNotEmptyNumber(value: String): Boolean =
        value.isNotEmpty() && value.all { it in '0'..'9' }

    private fun isValidClientToken(value: String?): Boolean = value == null || value.length >= 64

    class Validation(
        val validUrlChat: Boolean,
        val validUrlOfflineForm: Boolean,
        val validCompanyId: Boolean,
        val validChannelId: Boolean,
        val validClientToken: Boolean,
        val validClientEmail: Boolean,
        val validClientPhoneNumber: Boolean
    ) {
        fun isAllValid(): Boolean = validUrlChat
                && validUrlOfflineForm
                && validCompanyId
                && validChannelId
                && validClientEmail
                && validClientPhoneNumber

        override fun toString(): String = "Validation(validUrlChat=$validUrlChat, " +
                "validUrlOfflineForm=$validUrlOfflineForm, " +
                "validCompanyId=$validCompanyId, " +
                "validChannelId=$validChannelId, " +
                "validClientToken=$validClientToken, " +
                "validClientEmail=$validClientEmail, " +
                "validClientPhoneNumber=$validClientPhoneNumber)"
    }
}