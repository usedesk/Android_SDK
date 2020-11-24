package ru.usedesk.chat_sdk.external

import android.content.Context
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.external.service.notifications.UsedeskNotificationsServiceFactory
import ru.usedesk.chat_sdk.internal.di.InstanceBox

object UsedeskChatSdk {
    private var instanceBox: InstanceBox? = null
    private var configuration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory = UsedeskNotificationsServiceFactory()
    fun init(appContext: Context,
             actionListener: IUsedeskActionListener): IUsedeskChat {
        if (instanceBox == null) {
            checkConfiguration()
            instanceBox = InstanceBox(appContext, configuration!!, actionListener)
        }
        return instanceBox!!.usedeskChatSdk
    }

    val instance: IUsedeskChat
        get() {
            if (instanceBox == null) {
                throw RuntimeException("Must call UsedeskChatSdk.init(...) before")
            }
            return instanceBox!!.usedeskChatSdk
        }

    fun release() {
        if (instanceBox != null) {
            instanceBox!!.release()
            instanceBox = null
        }
    }

    fun setConfiguration(usedeskChatConfiguration: UsedeskChatConfiguration) {
        if (!usedeskChatConfiguration.isValid) {
            throw RuntimeException("Invalid configuration")
        }
        configuration = usedeskChatConfiguration
    }

    fun startService(context: Context) {
        checkConfiguration()
        notificationsServiceFactory.startService(context, configuration!!)
    }

    fun stopService(context: Context) {
        notificationsServiceFactory.stopService(context)
    }

    private fun checkConfiguration() {
        if (configuration == null) {
            throw RuntimeException("Call UsedeskChatSdk.setConfiguration(...) before")
        }
    }

    fun setNotificationsServiceFactory(usedeskNotificationsServiceFactory: UsedeskNotificationsServiceFactory) {
        notificationsServiceFactory = usedeskNotificationsServiceFactory
    }
}