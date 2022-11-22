package ru.usedesk.chat_sdk.service.notifications

import android.content.Context
import android.content.Intent
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService.Companion.USEDESK_CHAT_CONFIGURATION_KEY

open class UsedeskNotificationsServiceFactory {

    protected open val serviceClass: Class<*> = UsedeskNotificationsService::class.java

    open fun startService(
        context: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration
    ) {
        context.startService(
            Intent(context, serviceClass).apply {
                putExtra(USEDESK_CHAT_CONFIGURATION_KEY, usedeskChatConfiguration)
            }
        )
    }

    open fun stopService(context: Context) {
        context.stopService(Intent(context, serviceClass))
    }
}