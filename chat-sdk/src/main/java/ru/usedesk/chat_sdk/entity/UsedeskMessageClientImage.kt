package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import java.util.*

class UsedeskMessageClientImage(
        id: Long,
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val status: UsedeskMessageClient.Status
) : UsedeskMessageImage(id, calendar, usedeskFile), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_IMAGE
}