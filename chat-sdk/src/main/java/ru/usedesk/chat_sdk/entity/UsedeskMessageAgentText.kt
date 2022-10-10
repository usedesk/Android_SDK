package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageAgentText(
    id: Long,
    createdAt: Calendar,
    text: String,
    convertedText: String,
    val buttons: List<UsedeskMessageButton>,
    val fields: List<UsedeskMessageField>,
    val feedbackNeeded: Boolean,
    val feedback: UsedeskFeedback?,
    override val name: String,
    override val avatar: String
) : UsedeskMessageText(
    id,
    createdAt,
    text,
    convertedText
), UsedeskMessageAgent