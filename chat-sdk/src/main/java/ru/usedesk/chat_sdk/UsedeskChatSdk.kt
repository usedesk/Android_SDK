package ru.usedesk.chat_sdk

import android.content.Context
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.di.UsedeskCustom
import ru.usedesk.chat_sdk.di.chat.ChatComponent
import ru.usedesk.chat_sdk.di.common.CommonChatComponent
import ru.usedesk.chat_sdk.di.preparation.PreparationComponent
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.domain.IUsedeskPreparation
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory

object UsedeskChatSdk {

    const val MAX_FILE_SIZE_MB = 128
    const val MAX_FILE_SIZE = MAX_FILE_SIZE_MB * 1024 * 1024

    private var chatConfiguration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory: UsedeskNotificationsServiceFactory? =
        null //TODO: remove this from sdk
    private var usedeskMessagesRepository = UsedeskCustom<IUsedeskMessagesRepository>()


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
    ): IUsedeskChat {
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
    fun getInstance(): IUsedeskChat? = ChatComponent.chatComponent?.chatInteractor

    @JvmStatic
    fun requireInstance(): IUsedeskChat = getInstance()
        ?: throw RuntimeException("Must call UsedeskChatSdk.init(...) before")

    /**
     * Release all resources of IUsedeskChat
     * @param force If it is false, resources will be released only if no one listener exists
     */
    @JvmStatic
    @JvmOverloads
    fun release(force: Boolean = true) {
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
    ): IUsedeskPreparation {
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

    @JvmStatic
    fun setNotificationsServiceFactory(
        notificationsServiceFactory: UsedeskNotificationsServiceFactory?
    ) {
        this.notificationsServiceFactory = notificationsServiceFactory
    }

    @JvmStatic
    fun setUsedeskMessagesRepository(usedeskMessagesRepository: IUsedeskMessagesRepository?) {
        this.usedeskMessagesRepository = UsedeskCustom(usedeskMessagesRepository)
    }

    @JvmStatic
    fun startService(context: Context) {
        notificationsServiceFactory?.startService(context, requireConfiguration())
    }

    @JvmStatic
    fun stopService(context: Context) {
        notificationsServiceFactory?.stopService(context)
    }
}