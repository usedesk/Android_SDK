package ru.usedesk.chat_sdk.external.service.notifications.presenter

import ru.usedesk.chat_sdk.external.entity.UsedeskChatItem

data class UsedeskNotificationsModel @JvmOverloads constructor(
        val message: UsedeskChatItem,
        val count: Int = 0
)