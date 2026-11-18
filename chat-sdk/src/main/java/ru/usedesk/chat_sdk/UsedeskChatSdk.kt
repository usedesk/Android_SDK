package ru.usedesk.chat_sdk

import android.content.Context
import ru.usedesk.chat_sdk.data.repository.messages.UsedeskMessagesRepository
import ru.usedesk.chat_sdk.di.UsedeskCustom
import ru.usedesk.chat_sdk.di.chat.ChatComponent
import ru.usedesk.chat_sdk.di.common.CommonChatComponent
import ru.usedesk.chat_sdk.di.preparation.PreparationComponent
import ru.usedesk.chat_sdk.domain.UsedeskChat
import ru.usedesk.chat_sdk.domain.UsedeskPreparation
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.NotificationsServiceController
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory
import ru.usedesk.chat_sdk.service.notifications.view.NOTIFICATIONS_SERVICE_DEPRECATED

object UsedeskChatSdk {

    const val MAX_FILE_SIZE_MB = 128
    const val MAX_FILE_SIZE = MAX_FILE_SIZE_MB * 1024 * 1024

    private var chatConfiguration: UsedeskChatConfiguration? = null

    @Suppress("DEPRECATION")
    private var notificationsServiceFactory: UsedeskNotificationsServiceFactory? =
        null
    private var usedeskMessagesRepository = UsedeskCustom<UsedeskMessagesRepository>()


    @JvmStatic
    fun setConfiguration(chatConfiguration: UsedeskChatConfiguration) {
        val validation = chatConfiguration.validate()
        if (!validation.isAllValid()) {
            throw RuntimeException("Invalid chat configuration: $validation")
        }
        this.chatConfiguration = chatConfiguration
    }

    @JvmStatic
    fun requireConfiguration(): UsedeskChatConfiguration = chatConfiguration
        ?: throw RuntimeException("Must call UsedeskChatSdk.setConfiguration(...) before")

    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        chatConfiguration: UsedeskChatConfiguration = requireConfiguration()
    ): UsedeskChat {
        setConfiguration(chatConfiguration)
        val commonChatComponent = CommonChatComponent.open(
            context,
            chatConfiguration
        )
        return ChatComponent.open(
            commonChatComponent,
            usedeskMessagesRepository
        ).chatInteractor
    }

    @JvmStatic
    fun getInstance(): UsedeskChat? = ChatComponent.chatComponent?.chatInteractor

    @JvmStatic
    fun requireInstance(): UsedeskChat = getInstance()
        ?: throw RuntimeException("Must call UsedeskChatSdk.init(...) before")

    @JvmStatic
    @JvmOverloads
    fun release(force: Boolean = true) {
        if (force) NotificationsServiceController.stopAll()
        if (force || ChatComponent.chatComponent?.chatInteractor?.isNoListeners() == true) {
            ChatComponent.close()
            if (PreparationComponent.preparationComponent == null) {
                CommonChatComponent.close()
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun initPreparation(
        context: Context,
        chatConfiguration: UsedeskChatConfiguration = requireConfiguration()
    ): UsedeskPreparation {
        setConfiguration(chatConfiguration)
        val commonChatComponent = CommonChatComponent.open(
            context,
            chatConfiguration
        )
        return PreparationComponent.open(commonChatComponent).preparationInteractor
    }

    @JvmStatic
    fun releasePreparation() {
        PreparationComponent.close()
        if (ChatComponent.chatComponent == null) {
            CommonChatComponent.close()
        }
    }

    // Remove in 2027
    @Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
    @Suppress("DEPRECATION")
    @JvmStatic
    fun setNotificationsServiceFactory(
        notificationsServiceFactory: UsedeskNotificationsServiceFactory?
    ) {
        if (this.notificationsServiceFactory !== notificationsServiceFactory) {
            NotificationsServiceController.stopAll()
        }
        this.notificationsServiceFactory = notificationsServiceFactory
    }

    @JvmStatic
    fun setUsedeskMessagesRepository(usedeskMessagesRepository: UsedeskMessagesRepository?) {
        this.usedeskMessagesRepository = UsedeskCustom(usedeskMessagesRepository)
    }

    // Remove in 2027
    @Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
    @Suppress("DEPRECATION")
    @JvmStatic
    fun startService(context: Context) {
        chatConfiguration?.let { notificationsServiceFactory?.startService(context, it) }
    }

    // Remove in 2027
    @Deprecated(NOTIFICATIONS_SERVICE_DEPRECATED)
    @Suppress("DEPRECATION")
    @JvmStatic
    fun stopService(context: Context) {
        notificationsServiceFactory?.stopService(context)
    }
}
