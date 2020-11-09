package ru.usedesk.chat_sdk.external.entity.ticketitem

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class MessageClientImage(
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val received: Boolean
) : MessageFile(calendar, usedeskFile), MessageClient {
    override val type: Type = Type.TYPE_CLIENT_IMAGE
}