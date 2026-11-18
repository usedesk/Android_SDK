
package ru.usedesk.chat_sdk.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

@Parcelize
data class UsedeskChatConfiguration @JvmOverloads constructor(
    @SerializedName("urlChat") val urlChat: String = "https://pubsubsec.usedesk.ru",
    @SerializedName("urlChatApi") val urlChatApi: String = "https://secure.usedesk.ru",
    @SerializedName("companyId") val companyId: String,
    @SerializedName("channelId") val channelId: String,
    @SerializedName("messagesPageSize") val messagesPageSize: Int = 20,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("clientToken") val clientToken: String? = null,
    @SerializedName("clientEmail") val clientEmail: String? = null,
    @SerializedName("clientName") val clientName: String? = null,
    @SerializedName("clientNote") val clientNote: String? = null,
    @SerializedName("clientPhoneNumber") val clientPhoneNumber: Long? = null,
    @SerializedName("clientAdditionalId") val clientAdditionalId: String? = null,
    @SerializedName("clientInitMessage") val clientInitMessage: String? = null,
    @SerializedName("clientAvatar") val clientAvatar: String? = null,
    @SerializedName("cacheMessagesWithFile") val cacheMessagesWithFile: Boolean = true,
    @SerializedName("additionalFields") val additionalFields: Map<Long, String> = mapOf(),
    @SerializedName("additionalNestedFields") val additionalNestedFields: List<Map<Long, String>> = listOf(),
) : Parcelable {

    fun validate(): Validation = Validation(
        validUrlChat = UsedeskValidatorUtil.isValidUrlNecessary(urlChat),
        validUrlApi = UsedeskValidatorUtil.isValidUrlNecessary(urlChatApi),
        validCompanyId = companyId.isNotEmptyNumber(),
        validChannelId = channelId.isNotEmptyNumber(),
        validClientToken = clientToken.isValidClientToken(),
        validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail),
        validClientPhoneNumber = UsedeskValidatorUtil.isValidPhone(clientPhoneNumber?.toString()),
        validClientId = clientId.isNotBlank()
    )

    private fun String.isNotEmptyNumber(): Boolean = isNotEmpty() && all(Char::isDigit)

    private fun String?.isValidClientToken(): Boolean = this == null || length >= 64

    class Validation(
        val validUrlChat: Boolean,
        val validUrlApi: Boolean,
        val validCompanyId: Boolean,
        val validChannelId: Boolean,
        val validClientToken: Boolean,
        val validClientEmail: Boolean,
        val validClientPhoneNumber: Boolean,
        val validClientId: Boolean
    ) {
        fun isAllValid(): Boolean = validUrlChat
                && validUrlApi
                && validCompanyId
                && validChannelId
                && validClientEmail
                && validClientPhoneNumber
                && validClientId

        override fun toString(): String = "Validation(validUrlChat=$validUrlChat, " +
                "validUrlOfflineForm=$validUrlApi, " +
                "validCompanyId=$validCompanyId, " +
                "validChannelId=$validChannelId, " +
                "validClientToken=$validClientToken, " +
                "validClientEmail=$validClientEmail, " +
                "validClientPhoneNumber=$validClientPhoneNumber)"
    }
}