package ru.usedesk.common_sdk.utils

import java.text.SimpleDateFormat
import java.util.*

class UsedeskDateUtil {
    companion object {
        fun getLocalCalendar(pattern: String, dateValue: String): Calendar {
            return Calendar.getInstance().apply {
                time = SimpleDateFormat(pattern, Locale.getDefault())
                    .parse(dateValue)!!

                val hoursOffset = TimeZone.getDefault().rawOffset / 3600000
                add(Calendar.HOUR, hoursOffset)
            }
        }
    }
}