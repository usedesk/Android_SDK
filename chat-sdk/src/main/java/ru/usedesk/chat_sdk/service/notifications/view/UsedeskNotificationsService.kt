package ru.usedesk.chat_sdk.service.notifications.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.IntentCompat
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.R
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.UsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
import ru.usedesk.chat_sdk.service.notifications.NotificationsServiceController
import ru.usedesk.common_sdk.UsedeskLog

internal const val NOTIFICATIONS_SERVICE_DEPRECATED =
    "The local notifications service is legacy: on recent Android versions " +
            "a background service is not effective. Use Usedesk push notifications instead."

// Remove in 2027
@Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
@Suppress("DEPRECATION")
abstract class UsedeskNotificationsService : Service() {
    lateinit var notificationManager: NotificationManager
        private set

    protected open val showCloseButton = false

    protected open val channelId = "newUsedeskMessages"
    protected open val channelTitle: String by lazy { getString(R.string.usedesk_notification_channel_title) }
    protected open val fileMessage: String by lazy { getString(R.string.usedesk_notification_file_message) }

    private var model = Model()
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val modelMutex = Mutex()
    private var listeningChat: UsedeskChat? = null
    internal var isStopping = false
        private set
    internal var initializationError: RuntimeException? = null
        private set
    private var actionListener: UsedeskActionListener? = null

    private fun createActionListener() = object : UsedeskActionListener {
        override fun onModel(
            model: UsedeskChat.Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            newMessages.forEach { message ->
                if (message is UsedeskMessageOwner.Agent) {
                    updateModel(this) {
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

    private fun updateModel(listener: UsedeskActionListener? = null, onUpdate: Model.() -> Model) {
        mainScope.launch {
            modelMutex.withLock {
                if (!isStopping && (listener == null || actionListener === listener)) {
                    try {
                        model = model.onUpdate()
                        model.render()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: RuntimeException) {
                        stopAfterFailure(e)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationsServiceController.created(this)
        try {
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            registerNotification()
        } catch (e: RuntimeException) {
            initializationError = e
            if (this !is UsedeskForegroundNotificationsService) stopAfterFailure(e)
        }
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
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isStopping) return START_NOT_STICKY
        try {
            if (intent?.getBooleanExtra(UsedeskForegroundNotificationsService.STOP_SELF_KEY, false) == true) {
                NotificationsServiceController.stop(javaClass)
                return START_NOT_STICKY
            }
            val supplied = intent?.let {
                IntentCompat.getParcelableExtra(it, USEDESK_CHAT_CONFIGURATION_KEY, UsedeskChatConfiguration::class.java)
            }
            val configuration = NotificationsServiceController.configuration(this, supplied)
            if (configuration == null) {
                NotificationsServiceController.failed(this)
                return START_NOT_STICKY
            }
            startListening(configuration)
        } catch (e: RuntimeException) {
            stopAfterFailure(e)
        }
        return if (isStopping) START_NOT_STICKY else START_REDELIVER_INTENT
    }

    internal fun startListening(configuration: UsedeskChatConfiguration) {
        if (isStopping) return
        try {
            require(configuration.validate().isAllValid()) { "Invalid chat configuration" }
            val chat = UsedeskChatSdk.getInstance() ?: UsedeskChatSdk.init(this, configuration)
            if (listeningChat !== chat) {
                detachChat()
                model = Model()
                listeningChat = chat
                createActionListener().also {
                    actionListener = it
                    chat.addActionListener(it)
                }
            }
        } catch (e: RuntimeException) {
            stopAfterFailure(e)
        }
    }

    internal fun requestStop() {
        if (isStopping) return
        isStopping = true
        NotificationsServiceController.stopping(this)
        mainScope.cancel()
        detachChat()
        stopSelf()
    }

    internal fun stopAfterFailure(error: RuntimeException) {
        UsedeskLog.onLog("NotificationsService") { "Service stopped: $error" }
        NotificationsServiceController.failed(this)
    }

    private fun detachChat() {
        val chat = listeningChat ?: return
        listeningChat = null
        val listener = actionListener
        actionListener = null
        listener?.let(chat::removeActionListener)
        if (UsedeskChatSdk.getInstance() === chat) UsedeskChatSdk.release(false)
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
                is UsedeskMessage.Text -> HtmlCompat.fromHtml(
                    message.convertedText,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
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

        isStopping = true
        mainScope.cancel()
        detachChat()
        NotificationsServiceController.destroyed(this)
    }

    data class Model @JvmOverloads constructor(
        val message: UsedeskMessage? = null,
        val count: Int = 0
    )

    companion object {
        const val USEDESK_CHAT_CONFIGURATION_KEY = "usedeskChatConfigurationKey"
    }
}
