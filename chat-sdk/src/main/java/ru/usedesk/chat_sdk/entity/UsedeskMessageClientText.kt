package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageClientText @JvmOverloads constructor(
    id: Long,
    createdAt: Calendar,
    text: String,
    convertedText: String,
    override val status: UsedeskMessageClient.Status,
    override val localId: Long = id
) : UsedeskMessageText(
    id,
    createdAt,
    text,
    convertedText
), UsedeskMessageClient