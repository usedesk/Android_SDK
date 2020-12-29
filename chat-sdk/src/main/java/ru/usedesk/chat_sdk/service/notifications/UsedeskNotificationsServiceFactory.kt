package ru.usedesk.chat_sdk.service.notifications

import android.content.Context
import android.content.Intent
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService

open class UsedeskNotificationsServiceFactory {
    fun stopService(context: Context) {
        val intent = Intent(context, serviceClass)
        context.stopService(intent)
    }

    protected open val serviceClass: Class<*> = UsedeskNotificationsService::class.java

    fun startService(context: Context, usedeskChatConfiguration: UsedeskChatConfiguration) {
        val intent = Intent(context, serviceClass)
        usedeskChatConfiguration.serialize(intent)
        context.startService(intent)
    }
}