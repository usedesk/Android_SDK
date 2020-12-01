package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class UsedeskMessageAgentImage(
        id: String,
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val name: String,
        override val avatar: String
) : UsedeskMessageImage(id, calendar, usedeskFile), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_IMAGE
}