
package ru.usedesk.chat_sdk.service.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService.Companion.USEDESK_CHAT_CONFIGURATION_KEY

open class UsedeskNotificationsServiceFactory {

    protected open val serviceClass: Class<*> = UsedeskNotificationsService::class.java

    open fun startService(
        context: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration
    ) {
        val intent = Intent(context, serviceClass).apply {
            putExtra(USEDESK_CHAT_CONFIGURATION_KEY, usedeskChatConfiguration)
        }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> context.startForegroundService(intent)
            else -> context.startService(intent)
        }
    }

    open fun stopService(context: Context) {
        context.stopService(Intent(context, serviceClass))
    }
}