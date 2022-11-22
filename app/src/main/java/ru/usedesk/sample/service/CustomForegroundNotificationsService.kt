package ru.usedesk.sample.service

import android.app.PendingIntent
import android.content.Intent
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskForegroundNotificationsService
import ru.usedesk.sample.ui.main.MainActivity

class CustomForegroundNotificationsService : UsedeskForegroundNotificationsService() {
    override val serviceClass: Class<*> = CustomForegroundNotificationsService::class.java

    override val foregroundId = 137

    override fun getContentPendingIntent(): PendingIntent? = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        0
    )

    class Factory : UsedeskNotificationsServiceFactory() {
        override val serviceClass: Class<*> = CustomForegroundNotificationsService::class.java
    }
}