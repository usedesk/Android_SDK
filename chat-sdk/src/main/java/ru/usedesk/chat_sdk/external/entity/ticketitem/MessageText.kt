package ru.usedesk.chat_sdk.external.entity.ticketitem

import java.util.*

abstract class MessageText(
        calendar: Calendar,
        val text: String
) : ChatItem(calendar)