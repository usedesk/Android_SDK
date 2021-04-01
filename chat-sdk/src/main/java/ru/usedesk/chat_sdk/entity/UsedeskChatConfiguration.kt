package ru.usedesk.chat_sdk.entity

import android.content.Intent
import com.google.gson.Gson
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

data class UsedeskChatConfiguration @JvmOverloads constructor(
        val urlChat: String,
        val urlOfflineForm: String = "https://secure.usedesk.ru/",
        val urlToSendFile: String = "https://secure.usedesk.ru/uapi/v1/",
        val companyId: String,
        val channelId: String,
        val clientSignature: String? = null,
        val clientEmail: String? = null,
        val clientName: String? = null,
        val clientNote: String? = null,
        val clientPhoneNumber: Long? = null,
        val clientAdditionalId: Long? = null,
        val clientInitMessage: String? = null
) {
    fun getCompanyAndChannel(): String = "${companyId}_$channelId"

    fun serialize(intent: Intent) {
        intent.putExtra(CONFIGURATION_KEY, Gson().toJson(this))
    }

    fun validate(): Validation {
        return Validation(
                validUrlChat = UsedeskValidatorUtil.isValidUrlNecessary(urlChat),
                validUrlOfflineForm = UsedeskValidatorUtil.isValidUrlNecessary(urlOfflineForm),
                validUrlToSendFile = UsedeskValidatorUtil.isValidUrlNecessary(urlToSendFile),
                validCompanyId = isNotEmptyNumber(companyId),
                validChannelId = isNotEmptyNumber(channelId),
                validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail),
                validClientPhoneNumber = UsedeskValidatorUtil.isValidPhone(clientPhoneNumber)
        )
    }

    private fun isNotEmptyNumber(value: String): Boolean {
        return value.isNotEmpty() && value.all { it in '0'..'9' }
    }

    companion object {
        private const val CONFIGURATION_KEY = "usedeskChatConfigurationKey"

        @JvmStatic
        fun deserialize(intent: Intent): UsedeskChatConfiguration? {
            val json = intent.getStringExtra(CONFIGURATION_KEY)
            return if (json != null) {
                try {
                    Gson().fromJson(json, UsedeskChatConfiguration::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    class Validation(
            val validUrlChat: Boolean = false,
            val validUrlOfflineForm: Boolean = false,
            val validUrlToSendFile: Boolean = false,
            val validCompanyId: Boolean = false,
            val validChannelId: Boolean = false,
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
    }
}