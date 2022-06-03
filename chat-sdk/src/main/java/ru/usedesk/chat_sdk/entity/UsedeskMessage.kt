package ru.usedesk.chat_sdk.entity

import java.util.*

sealed class UsedeskMessage(
    val id: Long,
    val createdAt: Calendar
)