package ru.usedesk.chat_sdk.service.notifications.view

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ru.usedesk.chat_sdk.R
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgent
import ru.usedesk.chat_sdk.entity.UsedeskMessageText
import ru.usedesk.chat_sdk.service.notifications.presenter.UsedeskNotificationsModel
import ru.usedesk.chat_sdk.service.notifications.presenter.UsedeskNotificationsPresenter

abstract class UsedeskNotificationsService : Service() {
    private lateinit var presenter: UsedeskNotificationsPresenter

    lateinit var notificationManager: NotificationManager
        private set

    protected open val channelId = "newUsedeskMessages"
    protected open val channelTitle = "Messages from operator"
    protected open val fileMessage = "Sent the file"

    protected open val showCloseButton = false

    override fun onCreate() {
        super.onCreate()

        presenter = UsedeskNotificationsPresenter()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        registerNotification()
    }

    private fun registerNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelTitle,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val chatConfiguration = intent.getParcelableExtra<UsedeskChatConfiguration>(
            USEDESK_CHAT_CONFIGURATION_KEY
        )
        if (chatConfiguration != null) {
            UsedeskChatSdk.setConfiguration(chatConfiguration)
            UsedeskChatSdk.init(this)

            presenter.init {
                renderModel(it)
            }
        } else {
            stopSelf(startId)
        }

        return START_STICKY
    }

    private fun renderModel(model: UsedeskNotificationsModel?) {
        model?.also {
            createNotification(it)?.also { notification ->
                showNotification(notification)
            }
        }
    }

    protected abstract fun showNotification(notification: Notification)

    protected open fun getContentPendingIntent(): PendingIntent? = null
    protected open fun getDeletePendingIntent(): PendingIntent? = null
    protected open fun getClosePendingIntent(): PendingIntent? = null

    protected open fun createNotification(model: UsedeskNotificationsModel): Notification? {
        return if (model.message is UsedeskMessageAgent) {
            var title = model.message.name
            val text = if (model.message is UsedeskMessageText) {
                model.message.text
            } else {
                fileMessage
            }
            if (model.count > 1) {
                title += " (" + model.count + ")"
            }
            NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(android.R.drawable.ic_dialog_email)
                setContentTitle(title)
                setContentText(text)
                setContentIntent(getContentPendingIntent())
                setDeleteIntent(getDeletePendingIntent())
                if (showCloseButton) {
                    addAction(
                        android.R.drawable.ic_delete,
                        getString(R.string.usedesk_close),
                        getClosePendingIntent()
                    )
                }
            }.build().apply {
                flags = flags or Notification.FLAG_AUTO_CANCEL
            }
        } else {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onClear()
        UsedeskChatSdk.release(false)
    }

    companion object {
        const val USEDESK_CHAT_CONFIGURATION_KEY = "usedeskChatConfigurationKey"
    }
}