package ru.usedesk.chat_sdk.external.entity.ticketitem

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

abstract class MessageFile(
        calendar: Calendar,
        val file: UsedeskFile
) : ChatItem(calendar)