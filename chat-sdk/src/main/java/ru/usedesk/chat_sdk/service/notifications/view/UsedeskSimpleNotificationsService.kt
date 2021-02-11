package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification

abstract class UsedeskSimpleNotificationsService : UsedeskNotificationsService() {
    override fun showNotification(notification: Notification) {
        notificationManager.notify(5, notification)
    }
}