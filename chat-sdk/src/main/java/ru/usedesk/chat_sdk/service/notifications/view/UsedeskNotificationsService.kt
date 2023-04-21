
package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.Html
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.R
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner

abstract class UsedeskNotificationsService : Service() {
    lateinit var notificationManager: NotificationManager
        private set

    protected open val showCloseButton = false

    protected open val channelId = "newUsedeskMessages"
    protected open val channelTitle: String by lazy { getString(R.string.usedesk_notification_channel_title) }
    protected open val fileMessage: String by lazy { getString(R.string.usedesk_notification_file_message) }

    private var model = Model()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val modelMutex = Mutex()
    private val actionListener = object : IUsedeskActionListener {
        override fun onModel(
            model: IUsedeskChat.Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            newMessages.forEach { message ->
                if (message is UsedeskMessageOwner.Agent) {
                    updateModel {
                        copy(
                            message = message,
                            count = when (this@UsedeskNotificationsService.model.message) {
                                null -> 1
                                else -> this@UsedeskNotificationsService.model.count + 1
                            }
                        )
                    }
                }
            }
        }
    }

    private fun updateModel(onUpdate: Model.() -> Model) {
        mainScope.launch {
            modelMutex.withLock {
                model = model.onUpdate()
                model.render()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val chatConfiguration = intent?.getParcelableExtra<UsedeskChatConfiguration>(
            USEDESK_CHAT_CONFIGURATION_KEY
        )
        when {
            chatConfiguration != null -> {
                UsedeskChatSdk.setConfiguration(chatConfiguration)
                UsedeskChatSdk.init(this).apply {
                    addActionListener(actionListener)
                }
            }
            else -> stopSelf(startId)
        }

        return START_REDELIVER_INTENT
    }

    private fun Model.render() {
        createNotification()?.also(this@UsedeskNotificationsService::showNotification)
    }

    protected abstract fun showNotification(notification: Notification)

    protected open fun getContentPendingIntent(): PendingIntent? = null
    protected open fun getDeletePendingIntent(): PendingIntent? = null
    protected open fun getClosePendingIntent(): PendingIntent? = null

    protected open fun Model.createNotification(): Notification? = when (message) {
        is UsedeskMessageOwner.Agent -> {
            var title = message.name
            val text = when (message) {
                is UsedeskMessage.Text -> Html.fromHtml(message.convertedText)
                else -> fileMessage
            }
            if (model.count > 1) {
                title += " (${model.count})"
            }
            NotificationCompat.Builder(
                this@UsedeskNotificationsService,
                channelId
            ).apply {
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
        }
        else -> null
    }

    protected fun resetModel() {
        updateModel { Model() }
    }

    override fun onDestroy() {
        super.onDestroy()

        UsedeskChatSdk.requireInstance().removeActionListener(actionListener)
        UsedeskChatSdk.release(false)
    }

    data class Model @JvmOverloads constructor(
        val message: UsedeskMessage? = null,
        val count: Int = 0
    )

    companion object {
        const val USEDESK_CHAT_CONFIGURATION_KEY = "usedeskChatConfigurationKey"
    }
}