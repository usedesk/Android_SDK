package ru.usedesk.chat_sdk.external.entity.chat

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class UsedeskMessageClientFile(
        calendar: Calendar,
        file: UsedeskFile,
        override val received: Boolean
) : UsedeskMessageFile(calendar, file), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_FILE
}