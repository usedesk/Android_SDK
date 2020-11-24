package ru.usedesk.chat_sdk.internal.domain.entity

import ru.usedesk.chat_sdk.external.entity.UsedeskMessageType

abstract class BaseMessage(
        val id: String? = null,
        val type: UsedeskMessageType? = null,
        open val text: String? = null,
        val operator: String? = null,
        val createdAt: String? = null,
        val name: String? = null,
        val chat: Any? = null,
        val file: UsedeskFile? = null
) {

    constructor(baseMessage: BaseMessage) : this(
            baseMessage.id,
            baseMessage.type,
            baseMessage.text,
            baseMessage.operator,
            baseMessage.createdAt,
            baseMessage.name,
            baseMessage.chat,
            baseMessage.file
    )
}