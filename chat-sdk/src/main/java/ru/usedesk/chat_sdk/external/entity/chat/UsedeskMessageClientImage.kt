package ru.usedesk.chat_sdk.external.entity.chat

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

class UsedeskMessageClientImage(
        calendar: Calendar,
        usedeskFile: UsedeskFile,
        override val received: Boolean
) : UsedeskMessageImage(calendar, usedeskFile), UsedeskMessageClient {
    override val type: Type = Type.TYPE_CLIENT_IMAGE
}