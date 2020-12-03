package ru.usedesk.chat_sdk.entity

import android.content.Intent
import ru.usedesk.common_sdk.UsedeskValidatorUtil.isValidEmailNecessary
import ru.usedesk.common_sdk.UsedeskValidatorUtil.isValidPhone

data class UsedeskChatConfiguration @JvmOverloads constructor(
        val companyId: String,//TODO: а почему вдруг String а не Long?
        val email: String,
        val url: String,
        val offlineFormUrl: String,
        val clientName: String? = null,
        val clientPhoneNumber: Long? = null,
        val clientAdditionalId: Long? = null,
        val initClientMessage: String? = null
) {

    fun serialize(intent: Intent) {
        intent.putExtra(COMPANY_ID_KEY, companyId)
        intent.putExtra(EMAIL_KEY, email)
        intent.putExtra(URL_KEY, url)
        intent.putExtra(OFFLINE_FORM_URL_KEY, offlineFormUrl)
        intent.putExtra(NAME_KEY, clientName)
        intent.putExtra(PHONE_KEY, clientPhoneNumber)
        intent.putExtra(ADDITIONAL_ID_KEY, clientAdditionalId)
    }

    fun isValid(): Boolean {
        val phoneNumber = clientPhoneNumber?.toString()
        return isValidPhone(phoneNumber) && isValidEmailNecessary(email)
    }

    companion object {
        private const val COMPANY_ID_KEY = "companyIdKey"
        private const val EMAIL_KEY = "emailKey"
        private const val URL_KEY = "urlKey"
        private const val OFFLINE_FORM_URL_KEY = "offlineFormUrlKey"
        private const val NAME_KEY = "nmeKey"
        private const val PHONE_KEY = "phoneKey"
        private const val ADDITIONAL_ID_KEY = "additionalIdKey"

        @JvmOverloads
        @JvmStatic
        fun deserialize(intent: Intent,
                        keyPrefix: String = "usedesk"): UsedeskChatConfiguration? {
            val additionalId = intent.getLongExtra(keyPrefix + ADDITIONAL_ID_KEY, 0)
            val phone = intent.getLongExtra(keyPrefix + PHONE_KEY, 0)
            val companyId = intent.getStringExtra(keyPrefix + COMPANY_ID_KEY)
            val email = intent.getStringExtra(keyPrefix + EMAIL_KEY)
            val url = intent.getStringExtra(keyPrefix + URL_KEY)
            val offlineFormUrl = intent.getStringExtra(keyPrefix + OFFLINE_FORM_URL_KEY)
            val name = intent.getStringExtra(keyPrefix + NAME_KEY)

            return if (companyId != null
                    && email != null
                    && url != null
                    && offlineFormUrl != null
                    && name != null) {
                UsedeskChatConfiguration(companyId,
                        email,
                        url,
                        offlineFormUrl,
                        name,
                        phone,
                        additionalId)
            } else {
                null
            }
        }
    }
}