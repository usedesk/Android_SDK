package ru.usedesk.chat_sdk.external.entity.chat

import java.util.*

class UsedeskMessageClientText(
        calendar: Calendar,
        text: String,
        override val received: Boolean
) : UsedeskMessageText(calendar, text), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_TEXT
}