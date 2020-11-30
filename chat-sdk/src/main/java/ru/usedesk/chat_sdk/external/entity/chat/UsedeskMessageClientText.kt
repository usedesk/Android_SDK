package ru.usedesk.chat_sdk.external.entity.chat

import java.util.*

class UsedeskMessageClientText(
        calendar: Calendar,
        text: String,
        html: String
) : UsedeskMessageText(calendar, text, html), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_TEXT
}