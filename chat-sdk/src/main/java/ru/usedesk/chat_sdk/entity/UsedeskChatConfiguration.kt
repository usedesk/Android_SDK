
package ru.usedesk.chat_sdk.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

@Parcelize
data class UsedeskChatConfiguration @JvmOverloads constructor(
    val urlChat: String = "https://pubsubsec.usedesk.ru",
    val urlChatApi: String = "https://secure.usedesk.ru",
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

    fun validate(): Validation = Validation(
        validUrlChat = UsedeskValidatorUtil.isValidUrlNecessary(urlChat),
        validUrlApi = UsedeskValidatorUtil.isValidUrlNecessary(urlChatApi),
        validCompanyId = companyId.isNotEmptyNumber(),
        validChannelId = channelId.isNotEmptyNumber(),
        validClientToken = clientToken.isValidClientToken(),
        validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail),
        validClientPhoneNumber = UsedeskValidatorUtil.isValidPhone(clientPhoneNumber?.toString())
    )

    internal fun userKey() =
        "${companyId}_${channelId}_${clientEmail}_${clientPhoneNumber}_${clientName}"

    private fun String.isNotEmptyNumber(): Boolean = isNotEmpty() && all(Char::isDigit)

    private fun String?.isValidClientToken(): Boolean = this == null || length >= 64

    class Validation(
        val validUrlChat: Boolean,
        val validUrlApi: Boolean,
        val validCompanyId: Boolean,
        val validChannelId: Boolean,
        val validClientToken: Boolean,
        val validClientEmail: Boolean,
        val validClientPhoneNumber: Boolean
    ) {
        fun isAllValid(): Boolean = validUrlChat
                && validUrlApi
                && validCompanyId
                && validChannelId
                && validClientEmail
                && validClientPhoneNumber

        override fun toString(): String = "Validation(validUrlChat=$validUrlChat, " +
                "validUrlOfflineForm=$validUrlApi, " +
                "validCompanyId=$validCompanyId, " +
                "validChannelId=$validChannelId, " +
                "validClientToken=$validClientToken, " +
                "validClientEmail=$validClientEmail, " +
                "validClientPhoneNumber=$validClientPhoneNumber)"
    }
}