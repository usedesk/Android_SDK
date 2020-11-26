package ru.usedesk.chat_sdk.external.entity.chat

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import java.util.*

abstract class UsedeskMessageFile(
        calendar: Calendar,
        val file: UsedeskFile
) : UsedeskChatItem(calendar)