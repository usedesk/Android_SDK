package ru.usedesk.chat_sdk.external.entity.chat

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class UsedeskMessageAgentFile(
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val name: String,
        override val avatar: String
) : UsedeskMessageFile(calendar, usedeskFile), UsedeskMessageAgent {
    override val type: Type = Type.TYPE_AGENT_FILE
}