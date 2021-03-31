package ru.usedesk.sample.service

import android.app.PendingIntent
import android.content.Intent
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskSimpleNotificationsService
import ru.usedesk.sample.ui.main.MainActivity

class CustomSimpleNotificationsService : UsedeskSimpleNotificationsService() {
    override fun getContentPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    override fun getDeletePendingIntent(): PendingIntent? = null

    class Factory : UsedeskNotificationsServiceFactory() {
        override val serviceClass: Class<*> = CustomSimpleNotificationsService::class.java
    }
}