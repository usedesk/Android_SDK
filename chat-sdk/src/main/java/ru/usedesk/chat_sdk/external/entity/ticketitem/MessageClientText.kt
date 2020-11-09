package ru.usedesk.chat_sdk.external.entity.ticketitem

import java.util.*

class MessageClientText(
        calendar: Calendar,
        text: String,
        override val received: Boolean
) : MessageText(calendar, text), MessageClient {
    override val type: Type = Type.TYPE_CLIENT_TEXT
}