package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import java.util.*

class UsedeskMessageAgentImage(
        id: Long,
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val name: String,
        override val avatar: String
) : UsedeskMessageImage(id, calendar, usedeskFile), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_IMAGE
}