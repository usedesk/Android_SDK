package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import java.util.*

class UsedeskMessageClientFile(
        id: Long,
        calendar: Calendar,
        file: UsedeskFile
) : UsedeskMessageFile(id, calendar, file), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_FILE
}