package ru.usedesk.chat_sdk.external.entity.ticketitem

import java.util.*

class MessageAgentText(
        calendar: Calendar,
        text: String,
        override val name: String,
        override val avatar: String
) : MessageText(calendar, text), MessageAgent {
    override val type: Type = Type.TYPE_AGENT_TEXT
}