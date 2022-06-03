package ru.usedesk.chat_sdk.entity

import java.util.*

sealed class UsedeskMessageText(
    id: Long,
    createdAt: Calendar,
    val text: String,
    val convertedText: String
) : UsedeskMessage(id, createdAt)