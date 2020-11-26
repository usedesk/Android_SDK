package ru.usedesk.chat_sdk.external.entity.chat

import java.util.*

class UsedeskMessageAgentText(
        calendar: Calendar,
        text: String,
        override val name: String,
        override val avatar: String
) : UsedeskMessageText(calendar, text), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_TEXT
}