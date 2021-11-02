package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageClientVideo @JvmOverloads constructor(
    id: Long,
    createdAt: Calendar,
    file: UsedeskFile,
    override val status: UsedeskMessageClient.Status,
    override val localId: Long = id
) : UsedeskMessageFile(id, createdAt, file), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_VIDEO
}