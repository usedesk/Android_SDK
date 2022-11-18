package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal class SetClientResponse(
    private val clientId: Long
) : UsedeskApiError()