package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageAgentText(
    id: Long,
    createdAt: Calendar,
    text: String,
    val buttons: List<UsedeskMessageButton>,
    val feedbackNeeded: Boolean,
    val feedback: UsedeskFeedback?,
    override val name: String,
    override val avatar: String
) : UsedeskMessageText(id, createdAt, text), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_TEXT
}