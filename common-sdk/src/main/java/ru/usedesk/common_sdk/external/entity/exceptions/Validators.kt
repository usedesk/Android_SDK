package ru.usedesk.common_sdk.external.entity.exceptions

import android.util.Patterns

object Validators {
    fun isValidUrl(text: String?): Boolean {
        return text == null || isValidUrlNecessary(text)
    }

    fun isValidUrlNecessary(text: String?): Boolean {
        return text != null && Patterns.WEB_URL.matcher(text).matches()
    }

    fun isValidEmailNecessary(text: String?): Boolean {
        return text != null && Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }

    fun isValidPhonePhone(text: String?): Boolean {
        return text == null
                || text.isEmpty()
                || text == "+" || isValidPhoneNecessary(text)
    }

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