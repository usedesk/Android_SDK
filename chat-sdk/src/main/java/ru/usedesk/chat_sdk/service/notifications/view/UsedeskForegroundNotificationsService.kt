package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import ru.usedesk.chat_sdk.R
import ru.usedesk.chat_sdk.service.notifications.NotificationsServiceController
import ru.usedesk.common_sdk.UsedeskLog

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
        val notification = try {
            initializationError?.let { throw it }
            require(foregroundId != 0) { "Foreground notification ID must not be zero" }
            createStartNotification().also(::validateNotification)
        } catch (e: RuntimeException) {
            finishInvalidStartup(e)
            return
        }
        try {
            enterForeground(foregroundId, notification)
        } catch (e: RuntimeException) {
            stopAfterFailure(e)
        }
    }

    private fun finishInvalidStartup(error: RuntimeException) {
        try {
            val channel = "usedesk_service_startup"
            if (Build.VERSION.SDK_INT >= 26) {
                notificationManager.createNotificationChannel(NotificationChannel(
                    channel, getString(R.string.usedesk_chat_with_support), NotificationManager.IMPORTANCE_LOW
                ))
            }
            val notification = NotificationCompat.Builder(this, channel)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(getString(R.string.usedesk_chat_with_support))
                .build()
            enterForeground(0x555344, notification)
        } catch (e: RuntimeException) {
            UsedeskLog.onLog("NotificationsService") { "Fallback promotion rejected: $e" }
        }
        stopAfterFailure(error)
    }

    override fun showNotification(notification: Notification) {
        if (!isStopping) {
            validateNotification(notification)
            notificationManager.notify(foregroundId, notification)
        }
    }

    private fun validateNotification(notification: Notification) {
        require(notification.smallIcon != null) { "Notification must have a small icon" }
        require(notification.smallIcon.loadDrawable(this) != null) { "Notification icon must be loadable" }
        if (Build.VERSION.SDK_INT >= 26) {
            require(!notification.channelId.isNullOrEmpty() &&
                    notificationManager.getNotificationChannel(notification.channelId) != null) {
                "Notification channel must be registered"
            }
        }
    }

    private fun enterForeground(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        } else {
            startForeground(
                id,
                notification
            )
        }
    }

    override fun onTimeout(startId: Int) {
        NotificationsServiceController.stop(javaClass)
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        NotificationsServiceController.stop(javaClass)
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
