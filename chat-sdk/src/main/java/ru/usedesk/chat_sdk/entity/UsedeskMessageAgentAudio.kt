package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageAgentAudio(
    id: Long,
    createdAt: Calendar,
    usedeskFile: UsedeskFile,
    override val name: String,
    override val avatar: String
) : UsedeskMessageFile(id, createdAt, usedeskFile), UsedeskMessageAgent