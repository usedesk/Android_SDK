package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import java.util.*

abstract class UsedeskMessageFile(
        id: Long,
        calendar: Calendar,
        val file: UsedeskFile
) : UsedeskChatItem(id, calendar)