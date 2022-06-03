package ru.usedesk.chat_sdk.entity

import java.util.*

sealed class UsedeskMessageFile(
    id: Long,
    createdAt: Calendar,
    val file: UsedeskFile
) : UsedeskMessage(id, createdAt)