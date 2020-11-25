package ru.usedesk.chat_sdk.external.service.notifications.presenter

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage

data class UsedeskNotificationsModel @JvmOverloads constructor(
        val message: UsedeskMessage,
        val count: Int = 0
)