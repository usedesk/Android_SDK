package ru.usedesk.chat_sdk.external.entity.ticketitem

import java.util.*

class DateGroup(
        calendar: Calendar
) : ChatItem(calendar) {
    override val type: Type = Type.TYPE_DATE
}