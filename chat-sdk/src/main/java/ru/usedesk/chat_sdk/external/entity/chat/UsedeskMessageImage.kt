package ru.usedesk.chat_sdk.external.entity.chat

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

abstract class UsedeskMessageImage(
        calendar: Calendar,
        usedeskFile: UsedeskFile
) : UsedeskMessageFile(calendar, usedeskFile)