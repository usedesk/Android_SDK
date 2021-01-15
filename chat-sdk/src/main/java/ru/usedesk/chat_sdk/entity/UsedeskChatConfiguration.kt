package ru.usedesk.chat_sdk.entity

import android.content.Intent
import com.google.gson.Gson
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil.isValidEmailNecessary
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil.isValidPhone

data class UsedeskChatConfiguration @JvmOverloads constructor(
        val companyId: String,
        val email: String,
        val url: String,
        val offlineFormUrl: String,
        val clientName: String? = null,
        val clientPhoneNumber: Long? = null,
        val clientAdditionalId: Long? = null,
        val initClientMessage: String? = null
) {

    fun serialize(intent: Intent) {
        intent.putExtra(CONFIGURATION_KEY, Gson().toJson(this))
    }

    fun isValid(): Boolean {
        val phoneNumber = clientPhoneNumber?.toString()
        return isValidPhone(phoneNumber) && isValidEmailNecessary(email)
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
}