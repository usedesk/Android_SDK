package ru.usedesk.chat_sdk.external.service.notifications.view

import android.R
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.init
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.release
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.setConfiguration
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsModel
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsPresenter

abstract class UsedeskNotificationsService : Service() {
    private val presenter: UsedeskNotificationsPresenter
    protected var notificationManager: NotificationManager? = null
        private set
    private var messagesDisposable: Disposable? = null
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        registerNotification()
    }

    private fun registerNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(channelId,
                    channelTitle, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null) {
            setConfiguration(UsedeskChatConfiguration.deserialize(intent))
            init(this, presenter.actionListener)
            presenter.init()
            messagesDisposable = presenter.modelObservable
                    .subscribe { model: UsedeskNotificationsModel -> renderModel(model) }
        }
        return START_STICKY
    }

    private fun renderModel(model: UsedeskNotificationsModel) {
        showNotification(createNotification(model))
    }

    protected abstract fun showNotification(notification: Notification)
    protected open val contentPendingIntent: PendingIntent?
        protected get() = null
    protected open val deletePendingIntent: PendingIntent?
        protected get() = null

    protected fun createNotification(model: UsedeskNotificationsModel): Notification {
        var title = model.message.name
        val text = model.message.text
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
        return notification
    }

    override fun onDestroy() {
        super.onDestroy()
        if (messagesDisposable != null) {
            messagesDisposable!!.dispose()
        }
        release()
    }

    companion object {
        protected val channelId = "newUsedeskMessages"
            protected get() = Companion.field
        protected val channelTitle = "Messages from operator"
            protected get() = Companion.field
    }

    init {
        presenter = UsedeskNotificationsPresenter()
    }
}