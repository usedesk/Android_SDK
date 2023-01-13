package ru.usedesk.sample.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskSimpleNotificationsService
import ru.usedesk.sample.ui.main.MainActivity

class CustomSimpleNotificationsService : UsedeskSimpleNotificationsService() {
    override fun getContentPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> PendingIntent.FLAG_IMMUTABLE
                else -> 0
            }
        )
    }

    override fun getDeletePendingIntent(): PendingIntent? = null

    class Factory : UsedeskNotificationsServiceFactory() {
        override val serviceClass: Class<*> = CustomSimpleNotificationsService::class.java
    }
}