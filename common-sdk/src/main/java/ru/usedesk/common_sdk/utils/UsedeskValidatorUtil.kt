package ru.usedesk.common_sdk.utils

import android.util.Patterns

object UsedeskValidatorUtil {
    @JvmStatic
    fun isValidUrl(text: String?): Boolean {
        return text == null || text.isEmpty() || isValidUrlNecessary(text)
    }

    @JvmStatic
    fun isValidUrlNecessary(text: String?): Boolean {
        return text != null && text.isNotEmpty() && Patterns.WEB_URL.matcher(text).matches()
    }

    @JvmStatic
    fun isValidEmailNecessary(text: String?): Boolean {
        return text != null && Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }

    @JvmStatic
    fun isValidPhone(text: String?): Boolean {
        return text == null
                || text.isEmpty()
                || text == "+" || isValidPhoneNecessary(text)
    }

    @JvmStatic
    fun isValidPhoneNecessary(text: String?): Boolean {
        if (text == null) {
            return false
        }
        val phone = text.replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
        return phone.length in 7..13 && Patterns.PHONE
                .matcher(phone)
                .matches()
    }
}