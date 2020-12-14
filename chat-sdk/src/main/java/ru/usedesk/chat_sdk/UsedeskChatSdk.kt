package ru.usedesk.chat_sdk

import android.content.Context
import com.google.android.gms.security.ProviderInstaller
import ru.usedesk.chat_sdk.di.InstanceBox
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory

object UsedeskChatSdk {
    private var instanceBox: InstanceBox? = null
    private var configuration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory = UsedeskNotificationsServiceFactory()

    @JvmStatic
    fun init(appContext: Context, actionListener: IUsedeskActionListener): IUsedeskChat {
        try {
            ProviderInstaller.installIfNeeded(appContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (instanceBox == null) {
            instanceBox = InstanceBox(appContext, getConfiguration(), actionListener)
        }
        return instanceBox!!.usedeskChatSdk
    }

    @JvmStatic
    fun getInstance(): IUsedeskChat {
        if (instanceBox == null) {
            throw RuntimeException("Must call UsedeskChatSdk.init(...) before")
        }
        return instanceBox!!.usedeskChatSdk
    }

    @JvmStatic
    fun release() {
        if (instanceBox != null) {
            instanceBox!!.release()
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
        notificationsServiceFactory.startService(context, getConfiguration())
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
    fun getConfiguration(): UsedeskChatConfiguration {
        return configuration
                ?: throw RuntimeException("Call UsedeskChatSdk.setConfiguration(...) before")
    }
}