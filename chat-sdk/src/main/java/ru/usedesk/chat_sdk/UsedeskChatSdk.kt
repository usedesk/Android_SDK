package ru.usedesk.chat_sdk

import android.content.Context
import ru.usedesk.chat_sdk.di.InstanceBoxUsedesk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory

object UsedeskChatSdk {
    private var instanceBox: InstanceBoxUsedesk? = null
    private var configuration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory = UsedeskNotificationsServiceFactory()

    @JvmStatic
    fun init(appContext: Context): IUsedeskChat {
        return (instanceBox
                ?: InstanceBoxUsedesk(appContext, requireConfiguration()).also {
                    instanceBox = it
                }).usedeskChatSdk
    }

    @JvmStatic
    fun getInstance(): IUsedeskChat {
        return instanceBox?.usedeskChatSdk
                ?: throw RuntimeException("Must call UsedeskChatSdk.init(...) before")
    }

    /**
     * Завершает работу IUsedeskChat
     * При force == false завершит работу только в том случае, если не осталось слушателей
     */
    @JvmStatic
    @JvmOverloads
    fun release(force: Boolean = true) {
        instanceBox?.also {
            if (force || it.usedeskChatSdk.isNoSubscribers()) {
                it.release()
                instanceBox = null
            }
        }
    }

    @JvmStatic
    fun setConfiguration(usedeskChatConfiguration: UsedeskChatConfiguration) {
        if (!usedeskChatConfiguration.isValid()) {
            throw RuntimeException("Invalid configuration")
        }
        configuration = usedeskChatConfiguration
    }

    @JvmStatic
    fun startService(context: Context) {
        notificationsServiceFactory.startService(context, requireConfiguration())
    }

    @JvmStatic
    fun stopService(context: Context) {
        notificationsServiceFactory.stopService(context)
    }

    @JvmStatic
    fun setNotificationsServiceFactory(usedeskNotificationsServiceFactory: UsedeskNotificationsServiceFactory) {
        notificationsServiceFactory = usedeskNotificationsServiceFactory
    }

    @JvmStatic
    fun requireConfiguration(): UsedeskChatConfiguration {
        return configuration
                ?: throw RuntimeException("Must call UsedeskChatSdk.setConfiguration(...) before")
    }
}