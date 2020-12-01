package ru.usedesk.chat_sdk.external.entity

import java.util.*

class UsedeskMessageClientText(
        id: String,
        calendar: Calendar,
        text: String,
        html: String
) : UsedeskMessageText(id, calendar, text, html), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_TEXT
}