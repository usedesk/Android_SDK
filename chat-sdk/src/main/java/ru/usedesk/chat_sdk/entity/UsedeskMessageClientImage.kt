package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageClientImage @JvmOverloads constructor(
        id: Long,
        createdAt: Calendar,
        usedeskFile: UsedeskFile,
        override val status: UsedeskMessageClient.Status,
        override val localId: Long = id
) : UsedeskMessageFile(id, createdAt, usedeskFile), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_IMAGE
}