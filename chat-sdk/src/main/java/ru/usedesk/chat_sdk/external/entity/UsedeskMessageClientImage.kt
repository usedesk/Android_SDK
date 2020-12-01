package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class UsedeskMessageClientImage(
        id: String,
        calendar: Calendar,
        usedeskFile: UsedeskFile
) : UsedeskMessageImage(id, calendar, usedeskFile), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_IMAGE
}