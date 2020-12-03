package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import java.util.*

abstract class UsedeskMessageImage(
        id: Long,
        calendar: Calendar,
        usedeskFile: UsedeskFile
) : UsedeskMessageFile(id, calendar, usedeskFile)