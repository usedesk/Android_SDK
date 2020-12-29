package ru.usedesk.chat_sdk.service.notifications.presenter

import ru.usedesk.chat_sdk.entity.UsedeskMessage

data class UsedeskNotificationsModel @JvmOverloads constructor(
        val message: UsedeskMessage,
        val count: Int = 0
)