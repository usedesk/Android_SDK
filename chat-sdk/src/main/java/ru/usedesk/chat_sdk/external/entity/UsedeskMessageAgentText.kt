package ru.usedesk.chat_sdk.external.entity

import java.util.*

class UsedeskMessageAgentText(
        id: String,
        calendar: Calendar,
        text: String,
        html: String,
        override val name: String,
        override val avatar: String
) : UsedeskMessageText(id, calendar, text, html), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_TEXT
}