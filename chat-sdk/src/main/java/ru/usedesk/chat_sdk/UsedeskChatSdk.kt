package ru.usedesk.chat_sdk

import android.content.Context
import ru.usedesk.chat_sdk.di.InstanceBoxUsedesk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory

object UsedeskChatSdk {
    private var instanceBox: InstanceBoxUsedesk? = null
    private var configuration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory = UsedeskNotificationsServiceFactory()

    @JvmStatic
    fun init(appContext: Context, actionListener: IUsedeskActionListener): IUsedeskChat {
        return (instanceBox
                ?: InstanceBoxUsedesk(appContext, requireConfiguration(), actionListener).also {
                    instanceBox = it
                }).usedeskChatSdk
    }

    @JvmStatic
    fun getInstance(): IUsedeskChat {
        return instanceBox?.usedeskChatSdk
                ?: throw RuntimeException("Must call UsedeskChatSdk.init(...) before")
    }

    @JvmStatic
    fun release() {
        instanceBox?.also {
            it.release()
            instanceBox = null
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