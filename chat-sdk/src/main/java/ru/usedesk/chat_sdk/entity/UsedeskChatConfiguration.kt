package ru.usedesk.chat_sdk.entity

import android.content.Intent
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
        intent.putExtra(COMPANY_ID_KEY, companyId)
        intent.putExtra(EMAIL_KEY, email)
        intent.putExtra(URL_KEY, url)
        intent.putExtra(OFFLINE_FORM_URL_KEY, offlineFormUrl)
        intent.putExtra(NAME_KEY, clientName)
        intent.putExtraLongNullable(PHONE_KEY, clientPhoneNumber)
        intent.putExtraLongNullable(ADDITIONAL_ID_KEY, clientAdditionalId)
    }

    fun isValid(): Boolean {
        val phoneNumber = clientPhoneNumber?.toString()
        return isValidPhone(phoneNumber) && isValidEmailNecessary(email)
    }

    companion object {
        private const val COMPANY_ID_KEY = "usedeskCompanyIdKey"
        private const val EMAIL_KEY = "usedeskEmailKey"
        private const val URL_KEY = "usedeskUrlKey"
        private const val OFFLINE_FORM_URL_KEY = "usedeskOfflineFormUrlKey"
        private const val NAME_KEY = "usedeskNameKey"
        private const val PHONE_KEY = "usedeskPhoneKey"
        private const val ADDITIONAL_ID_KEY = "usedeskAdditionalIdKey"

        @JvmStatic
        fun deserialize(intent: Intent): UsedeskChatConfiguration? {
            val additionalId = intent.getLongExtraOrNull(ADDITIONAL_ID_KEY)
            val phone = intent.getLongExtraOrNull(PHONE_KEY)
            val companyId = intent.getStringExtra(COMPANY_ID_KEY)
            val email = intent.getStringExtra(EMAIL_KEY)
            val url = intent.getStringExtra(URL_KEY)
            val offlineFormUrl = intent.getStringExtra(OFFLINE_FORM_URL_KEY)
            val name = intent.getStringExtra(NAME_KEY)

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

        private fun Intent.getLongExtraOrNull(key: String): Long? {
            return if (hasExtra(key)) {
                return getLongExtra(key, 0)
            } else {
                null
            }
        }

        private fun Intent.putExtraLongNullable(key: String,
                                                value: Long?) {
            value?.also {
                putExtra(key, it)
            }
        }
    }
}