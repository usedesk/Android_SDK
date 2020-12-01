package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class UsedeskMessageClientFile(
        id: String,
        calendar: Calendar,
        file: UsedeskFile
) : UsedeskMessageFile(id, calendar, file), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_FILE
}