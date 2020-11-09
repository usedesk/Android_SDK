package ru.usedesk.chat_sdk.external.entity.ticketitem

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class MessageClientFile(
        calendar: Calendar,
        file: UsedeskFile,
        override val received: Boolean
) : MessageFile(calendar, file), MessageClient {
    override val type: Type = Type.TYPE_CLIENT_FILE
}