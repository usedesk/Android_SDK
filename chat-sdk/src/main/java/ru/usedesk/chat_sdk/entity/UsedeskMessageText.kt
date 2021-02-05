package ru.usedesk.chat_sdk.entity

import java.util.*

abstract class UsedeskMessageText(
        id: Long,
        createdAt: Calendar,
        val text: String
) : UsedeskMessage(id, createdAt)