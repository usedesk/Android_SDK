package ru.usedesk.chat_sdk.external.entity

import java.util.*

class UsedeskDateGroup(
        calendar: Calendar
) : UsedeskChatItem("", calendar) {
    override val type: Type = Type.TYPE_DATE
}