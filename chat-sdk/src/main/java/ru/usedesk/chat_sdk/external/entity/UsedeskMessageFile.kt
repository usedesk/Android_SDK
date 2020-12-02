package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

abstract class UsedeskMessageFile(
        id: Long,
        calendar: Calendar,
        val file: UsedeskFile
) : UsedeskChatItem(id, calendar)