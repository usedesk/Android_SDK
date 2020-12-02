package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

abstract class UsedeskMessageImage(
        id: Long,
        calendar: Calendar,
        usedeskFile: UsedeskFile
) : UsedeskMessageFile(id, calendar, usedeskFile)