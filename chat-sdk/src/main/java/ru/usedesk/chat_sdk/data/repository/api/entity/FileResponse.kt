package ru.usedesk.chat_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal class FileResponse(
    var status: Int?,
    var fileLink: String?,
    var size: String?,
    var id: String?,
    var type: String?,
    var name: String?
) : UsedeskApiError()