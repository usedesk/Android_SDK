
package ru.usedesk.common_sdk.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class UsedeskDateUtil {
    companion object {
        // Сервер отдаёт UTC, но 'Z' в шаблоне экранирована, поэтому зону задаём явно
        fun getLocalCalendar(pattern: String, dateValue: String): Calendar = Calendar.getInstance()
            .apply {
                time = SimpleDateFormat(pattern, Locale.ROOT)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                    .parse(dateValue)!!
            }
    }
}
