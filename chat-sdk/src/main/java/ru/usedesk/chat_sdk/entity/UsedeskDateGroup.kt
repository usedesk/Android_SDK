package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskDateGroup(
        calendar: Calendar
) : UsedeskMessage(0, calendar) {
    override val type: Type = Type.TYPE_DATE
}