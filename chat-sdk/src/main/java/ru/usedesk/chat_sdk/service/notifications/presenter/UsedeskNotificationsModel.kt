package ru.usedesk.chat_sdk.service.notifications.presenter

import ru.usedesk.chat_sdk.entity.UsedeskChatItem

data class UsedeskNotificationsModel @JvmOverloads constructor(
        val message: UsedeskChatItem,
        val count: Int = 0
)