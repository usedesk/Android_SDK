package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageClientText(
        id: Long,
        calendar: Calendar,
        text: String,
        override val status: UsedeskMessageClient.Status
) : UsedeskMessageText(id, calendar, text), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_TEXT
}