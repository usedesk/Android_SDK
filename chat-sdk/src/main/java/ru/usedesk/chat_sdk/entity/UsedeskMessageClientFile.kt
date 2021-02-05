package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageClientFile(
        id: Long,
        createdAt: Calendar,
        file: UsedeskFile,
        override val status: UsedeskMessageClient.Status
) : UsedeskMessageFile(id, createdAt, file), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_FILE
}