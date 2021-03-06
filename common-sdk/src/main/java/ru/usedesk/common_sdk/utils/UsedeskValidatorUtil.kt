package ru.usedesk.common_sdk.utils

import android.util.Patterns
import java.util.regex.Pattern

object UsedeskValidatorUtil {
    //Либо пустой, либо валидный
    private fun isValid(text: String?,
                        pattern: Pattern?,
                        customRule: (String) -> Boolean = { true }): Boolean {
        return text?.isEmpty() != false
                || (pattern?.matcher(text)?.matches() != false
                && customRule(text))
    }

    //Не пустой и валидный
    private fun isValidNecessary(text: String?,
                                 pattern: Pattern?,
                                 customRule: (String) -> Boolean = { true }): Boolean {
        return text?.isNotEmpty() == true
                && pattern?.matcher(text)?.matches() != false
                && customRule(text)
    }

    @JvmStatic
    fun isValidUrl(url: String?): Boolean {
        return isValid(url, Patterns.WEB_URL)
    }

    @JvmStatic
    fun isValidUrlNecessary(url: String?): Boolean {
        return isValidNecessary(url, Patterns.WEB_URL)
    }

    @JvmStatic
    fun isValidEmail(email: String?): Boolean {
        return isValid(email, Patterns.EMAIL_ADDRESS)
    }

    @JvmStatic
    fun isValidEmailNecessary(email: String?): Boolean {
        return isValidNecessary(email, Patterns.EMAIL_ADDRESS)
    }

    @JvmStatic
    fun isValidPhone(phone: Long?): Boolean {
        return isValid(phone?.toString(), Patterns.PHONE) {
            it.length in 10..17
        }
    }

    @JvmStatic
    fun isValidPhoneNecessary(phone: Long?): Boolean {
        return isValidNecessary(phone?.toString(), Patterns.PHONE) {
            it.length in 10..17
        }
    }
}