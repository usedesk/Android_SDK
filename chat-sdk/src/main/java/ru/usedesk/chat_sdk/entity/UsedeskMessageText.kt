package ru.usedesk.chat_sdk.entity

import java.util.*

abstract class UsedeskMessageText(
        id: Long,
        calendar: Calendar,
        val text: String,
        val html: String
) : UsedeskChatItem(id, calendar)