package ru.usedesk.common_sdk.utils

import android.util.Patterns
import java.util.regex.Pattern

object UsedeskValidatorUtil {
    //Либо пустой, либо валидный
    private fun isValid(
        text: String?,
        pattern: Pattern?,
        customRule: (String) -> Boolean = { true }
    ): Boolean = text?.isEmpty() != false
            || (pattern?.matcher(text)?.matches() != false
            && customRule(text))

    //Не пустой и валидный
    private fun isValidNecessary(
        text: String?,
        pattern: Pattern?,
        customRule: (String) -> Boolean = { true }
    ): Boolean = text?.isNotEmpty() == true
            && pattern?.matcher(text)?.matches() != false
            && customRule(text)

    @JvmStatic
    fun isValidUrl(url: String?): Boolean = isValid(url, Patterns.WEB_URL)

    @JvmStatic
    fun isValidUrlNecessary(url: String?): Boolean = isValidNecessary(url, Patterns.WEB_URL)

    @JvmStatic
    fun isValidEmail(email: String?): Boolean = isValid(email, Patterns.EMAIL_ADDRESS)

    @JvmStatic
    fun isValidEmailNecessary(email: String?): Boolean =
        isValidNecessary(email, Patterns.EMAIL_ADDRESS)

    @JvmStatic
    fun isValidPhone(phone: Long?): Boolean = isValid(phone?.toString(), Patterns.PHONE) {
        it.length in 10..17
    }

    @JvmStatic
    fun isValidPhoneNecessary(phone: Long?): Boolean =
        isValidNecessary(phone?.toString(), Patterns.PHONE) {
            it.length in 10..17
        }
}