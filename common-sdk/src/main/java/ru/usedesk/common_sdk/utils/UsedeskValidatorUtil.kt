
package ru.usedesk.common_sdk.utils

import android.util.Patterns
import java.util.regex.Pattern

object UsedeskValidatorUtil {
    //Empty or valid
    private fun isValid(
        text: String?,
        pattern: Pattern?,
        customRule: (String) -> Boolean = { true }
    ): Boolean = text?.isEmpty() != false
            || (pattern?.matcher(text)?.matches() != false
            && customRule(text))

    //Not empty and valid
    private fun isValidNecessary(
        text: String?,
        pattern: Pattern?,
        customRule: (String) -> Boolean = { true }
    ): Boolean = text?.isNotEmpty() == true
            && pattern?.matcher(text)?.matches() != false
            && customRule(text)

    @JvmStatic
    fun isValidUrl(url: String?) = isValid(url, Patterns.WEB_URL)

    @JvmStatic
    fun isValidUrlNecessary(url: String?) = isValidNecessary(url, Patterns.WEB_URL)

    @JvmStatic
    fun isValidEmail(email: String?) = isValid(email, Patterns.EMAIL_ADDRESS)

    @JvmStatic
    fun isValidEmailNecessary(email: String?) = isValidNecessary(email, Patterns.EMAIL_ADDRESS)

    @JvmStatic
    fun isValidPhone(phone: String?): Boolean = isValid(
        phone,
        Patterns.PHONE,
        customRule = { it.length in 10..17 }
    )

    @JvmStatic
    fun isValidPhoneNecessary(phone: String?): Boolean = isValidNecessary(
        phone,
        Patterns.PHONE,
        customRule = { it.length in 10..17 }
    )
}