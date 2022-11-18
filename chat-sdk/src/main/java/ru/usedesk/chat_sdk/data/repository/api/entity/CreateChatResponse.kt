package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal class CreateChatResponse(
    val status: String?,
    val chatId: Long?,
    val token: String?
) : UsedeskApiError()