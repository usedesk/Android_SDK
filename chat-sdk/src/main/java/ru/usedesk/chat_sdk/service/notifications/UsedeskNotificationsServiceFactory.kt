
package ru.usedesk.chat_sdk.service.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.view.NOTIFICATIONS_SERVICE_DEPRECATED
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService
import ru.usedesk.common_sdk.UsedeskLog

// Remove in 2027
@Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
@Suppress("DEPRECATION")
open class UsedeskNotificationsServiceFactory {

    protected open val serviceClass: Class<*> = UsedeskNotificationsService::class.java

    open fun startService(
        context: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration
    ) {
        if (UsedeskNotificationsService.started != null) {
            return
        }
        UsedeskNotificationsService.stopRequested = false
        val intent = Intent(context, serviceClass).apply {
            putExtra(
                UsedeskNotificationsService.USEDESK_CHAT_CONFIGURATION_KEY,
                usedeskChatConfiguration
            )
        }
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> context.startForegroundService(intent)
                else -> context.startService(intent)
            }
        } catch (e: IllegalStateException) {
            UsedeskLog.onLog("NotificationsService") { "Start rejected: $e" }
        } catch (e: SecurityException) {
            UsedeskLog.onLog("NotificationsService") { "Start rejected: $e" }
        }
    }

    open fun stopService(context: Context) {
        when (UsedeskNotificationsService.started) {
            null -> UsedeskNotificationsService.stopRequested = true
            else -> {
                UsedeskNotificationsService.started = null
                context.stopService(Intent(context, serviceClass))
            }
        }
    }
}
