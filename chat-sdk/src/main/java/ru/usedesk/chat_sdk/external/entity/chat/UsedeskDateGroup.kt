package ru.usedesk.chat_sdk.external.entity.chat

import java.util.*

class UsedeskDateGroup(
        calendar: Calendar
) : UsedeskChatItem(calendar) {
    override val type: Type = Type.TYPE_DATE
}