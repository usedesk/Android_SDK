package ru.usedesk.chat_sdk.external.entity.ticketitem

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class MessageAgentFile(
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val name: String,
        override val avatar: String
) : MessageFile(calendar, usedeskFile), MessageAgent {
    override val type: Type = Type.TYPE_AGENT_FILE
}