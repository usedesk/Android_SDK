
package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ru.usedesk.chat_sdk.R


abstract class UsedeskForegroundNotificationsService : UsedeskNotificationsService() {

    override val showCloseButton = true

    protected abstract val serviceClass: Class<*>
    protected abstract val foregroundId: Int

    protected open val notificationTitle: String by lazy { getString(R.string.usedesk_chat_with_support) }

    override fun onCreate() {
        super.onCreate()
        startForeground(foregroundId, createStartNotification())
    }

    override fun showNotification(notification: Notification) {
        stopForeground(true)
        startForeground(foregroundId, notification)
    }

    override fun getClosePendingIntent(): PendingIntent? = PendingIntent.getService(
        applicationContext,
        0,
        Intent(applicationContext, serviceClass).apply {
            putExtra(STOP_SELF_KEY, true)
        },
        PendingIntent.FLAG_CANCEL_CURRENT or when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> PendingIntent.FLAG_IMMUTABLE
            else -> 0
        }
    )

    protected open fun createStartNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(notificationTitle)
            .setContentIntent(getContentPendingIntent())
            .setDeleteIntent(getDeletePendingIntent())
            .addAction(
                android.R.drawable.ic_delete,
                getString(R.string.usedesk_close),
                getClosePendingIntent()
            )
            .build().apply {
                flags = flags or Notification.FLAG_AUTO_CANCEL
            }


    companion object {
        const val STOP_SELF_KEY = "stopSelfKey"
    }
}