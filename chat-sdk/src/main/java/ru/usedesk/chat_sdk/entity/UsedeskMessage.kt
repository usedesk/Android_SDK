package ru.usedesk.chat_sdk.entity

import java.util.*

abstract class UsedeskMessage(
    val id: Long,
    val createdAt: Calendar
) {
    abstract val type: Type

    enum class Type(val value: Int) {
        TYPE_AGENT_TEXT(1),
        TYPE_AGENT_IMAGE(2),
        TYPE_AGENT_VIDEO(3),
        TYPE_AGENT_AUDIO(4),
        TYPE_AGENT_FILE(5),
        TYPE_CLIENT_TEXT(6),
        TYPE_CLIENT_IMAGE(7),
        TYPE_CLIENT_VIDEO(8),
        TYPE_CLIENT_AUDIO(9),
        TYPE_CLIENT_FILE(10)
    }
}