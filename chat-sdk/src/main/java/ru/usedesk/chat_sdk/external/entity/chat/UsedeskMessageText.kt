package ru.usedesk.chat_sdk.external.entity.chat

import java.util.*

abstract class UsedeskMessageText(
        calendar: Calendar,
        val text: String,
        val html: String
) : UsedeskChatItem(calendar)