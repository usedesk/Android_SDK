package ru.usedesk.chat_sdk.service.notifications

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.view.NOTIFICATIONS_SERVICE_DEPRECATED
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskForegroundNotificationsService
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService
import ru.usedesk.common_sdk.UsedeskLog
import java.lang.reflect.Modifier

// Remove in 2027
@Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
@Suppress("DEPRECATION")
open class UsedeskNotificationsServiceFactory {

    protected open val serviceClass: Class<*> = UsedeskNotificationsService::class.java

    open fun startService(
        context: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration
    ) {
        try {
            require(UsedeskNotificationsService::class.java.isAssignableFrom(serviceClass) &&
                    !Modifier.isAbstract(serviceClass.modifiers)) { "A concrete notifications service is required" }
            require(usedeskChatConfiguration.validate().isAllValid()) { "Invalid chat configuration" }
            val info = context.packageManager.getServiceInfo(ComponentName(context, serviceClass), 0)
            require(info.enabled) { "Notifications service is disabled" }
            val foreground = UsedeskForegroundNotificationsService::class.java.isAssignableFrom(serviceClass)
            if (Build.VERSION.SDK_INT >= 28 && foreground) {
                require(context.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
                    "FOREGROUND_SERVICE permission is required"
                }
            }
            if (Build.VERSION.SDK_INT >= 34 && foreground) {
                require(info.foregroundServiceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING != 0) {
                    "Notifications service must declare the remoteMessaging foreground service type"
                }
                if (context.applicationInfo.targetSdkVersion >= 34) {
                    require(context.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING) == PackageManager.PERMISSION_GRANTED) {
                        "FOREGROUND_SERVICE_REMOTE_MESSAGING permission is required"
                    }
                }
            }
            NotificationsServiceController.start(context, serviceClass, usedeskChatConfiguration)
        } catch (e: Exception) {
            UsedeskLog.onLog("NotificationsService") { "Start rejected: $e" }
        }
    }

    open fun stopService(context: Context) {
        NotificationsServiceController.stop(serviceClass)
    }
}
