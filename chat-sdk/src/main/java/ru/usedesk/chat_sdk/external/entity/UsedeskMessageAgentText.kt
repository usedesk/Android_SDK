package ru.usedesk.chat_sdk.external.entity

import java.util.*

class UsedeskMessageAgentText(
        id: Long,
        calendar: Calendar,
        text: String,
        html: String,
        val buttons: List<UsedeskMessageButton>,
        val feedbackNeeded: Boolean,
        val feedback: UsedeskFeedback?,
        override val name: String,
        override val avatar: String
) : UsedeskMessageText(id, calendar, text, html), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_TEXT
}