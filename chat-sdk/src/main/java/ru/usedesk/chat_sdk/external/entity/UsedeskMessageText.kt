package ru.usedesk.chat_sdk.external.entity

import java.util.*

abstract class UsedeskMessageText(
        id: String,
        calendar: Calendar,
        val text: String,
        val html: String
) : UsedeskChatItem(id, calendar)