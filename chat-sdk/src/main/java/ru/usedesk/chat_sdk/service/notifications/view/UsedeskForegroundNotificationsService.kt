package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import ru.usedesk.chat_sdk.R


abstract class UsedeskForegroundNotificationsService : UsedeskNotificationsService() {

    override val showCloseButton = true

    protected abstract val serviceClass: Class<*>

    override fun onCreate() {
        super.onCreate()
        startForeground(foregroundId, createStartNotification())
    }

    override fun showNotification(notification: Notification) {
        stopForeground(true)
        startForeground(foregroundId, notification)
    }

    protected abstract val foregroundId: Int

    override fun getClosePendingIntent(): PendingIntent? {
        return PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, serviceClass).apply {
                putExtra(STOP_SELF_KEY, true)
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    protected open fun createStartNotification(): Notification {
        val title = "Чат с оператором"
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentIntent(getContentPendingIntent())
            .setDeleteIntent(getDeletePendingIntent())
            .addAction(
                android.R.drawable.ic_delete,
                getString(R.string.usedesk_close),
                getClosePendingIntent()
            )
            .build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        return notification
    }

    companion object {
        const val STOP_SELF_KEY = "stopSelfKey"
    }
}