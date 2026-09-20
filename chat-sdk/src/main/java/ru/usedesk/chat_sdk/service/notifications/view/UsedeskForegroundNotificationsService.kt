package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import ru.usedesk.chat_sdk.R

// Remove in 2027
@Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
@Suppress("DEPRECATION")
abstract class UsedeskForegroundNotificationsService : UsedeskNotificationsService() {

    override val showCloseButton = true

    protected abstract val serviceClass: Class<*>
    protected abstract val foregroundId: Int

    protected open val notificationTitle: String by lazy { getString(R.string.usedesk_chat_with_support) }

    override fun onCreate() {
        super.onCreate()
        startForeground(createStartNotification())
    }

    override fun showNotification(notification: Notification) {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        startForeground(notification)
    }

    private fun startForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                foregroundId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        } else {
            startForeground(
                foregroundId,
                notification
            )
        }
    }

    override fun getClosePendingIntent(): PendingIntent? = PendingIntent.getService(
        applicationContext,
        0,
        Intent(applicationContext, serviceClass).apply {
            putExtra(STOP_SELF_KEY, true)
        },
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
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