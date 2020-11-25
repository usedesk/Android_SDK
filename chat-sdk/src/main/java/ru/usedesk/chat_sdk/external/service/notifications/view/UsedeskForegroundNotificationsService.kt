package ru.usedesk.chat_sdk.external.service.notifications.view

import android.R
import android.app.Notification
import androidx.core.app.NotificationCompat

abstract class UsedeskForegroundNotificationsService : UsedeskNotificationsService() {
    override fun onCreate() {
        super.onCreate()
        startForeground(foregroundId, createStartNotification())
    }

    override fun showNotification(notification: Notification) {
        stopForeground(true)
        startForeground(foregroundId, notification)
    }

    protected abstract val foregroundId: Int

    protected open fun createStartNotification(): Notification {
        val title = "Чат с оператором"
        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        return notification
    }
}