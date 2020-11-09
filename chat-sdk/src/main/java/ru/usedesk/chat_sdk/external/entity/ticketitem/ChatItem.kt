package ru.usedesk.chat_sdk.external.entity.ticketitem

import java.util.*

abstract class ChatItem(
        val calendar: Calendar
) {
    abstract val type: Type

    enum class Type(val value: Int) {
        TYPE_AGENT_TEXT(1),
        TYPE_AGENT_IMAGE(2),
        TYPE_AGENT_FILE(3),
        TYPE_CLIENT_TEXT(4),
        TYPE_CLIENT_IMAGE(5),
        TYPE_CLIENT_FILE(6),
        TYPE_DATE(7)
    }
}