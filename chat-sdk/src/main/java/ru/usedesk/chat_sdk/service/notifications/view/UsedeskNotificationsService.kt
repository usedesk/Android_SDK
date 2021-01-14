package ru.usedesk.chat_sdk.service.notifications.view

import android.R
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.reactivex.disposables.Disposable
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

    private var messagesDisposable: Disposable? = null

    protected open val channelId = "newUsedeskMessages"
    protected open val channelTitle = "Messages from operator"
    protected open val fileMessage = "Sent the file"

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
        Thread.sleep(5000)
        val usedeskChatConfiguration = UsedeskChatConfiguration.deserialize(intent)

        if (usedeskChatConfiguration == null) {
            stopSelf(startId)
        } else {
            UsedeskChatSdk.setConfiguration(usedeskChatConfiguration)
            UsedeskChatSdk.init(this, presenter.actionListener)

            presenter.init()

            messagesDisposable = presenter.modelObservable.subscribe {
                renderModel(it)
            }
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
    protected open val contentPendingIntent: PendingIntent? = null
    protected open val deletePendingIntent: PendingIntent? = null

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
            val notification = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_dialog_email)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(contentPendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .build()
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
            notification
        } else {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        messagesDisposable?.also {
            it.dispose()
        }

        UsedeskChatSdk.release()
    }
}