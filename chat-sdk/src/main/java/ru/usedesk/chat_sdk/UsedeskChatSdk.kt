package ru.usedesk.chat_sdk

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.di.InstanceBoxUsedesk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory

object UsedeskChatSdk {

    const val MAX_FILE_SIZE_MB = 128
    const val MAX_FILE_SIZE = MAX_FILE_SIZE_MB * 1024 * 1024

    private val mutex = Mutex()
    private var instanceBox: InstanceBoxUsedesk? = null
    private var chatConfiguration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory: UsedeskNotificationsServiceFactory? =
        null //TODO: вынести функционал из sdk
    private var usedeskMessagesRepository: IUsedeskMessagesRepository? = null

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
    ): IUsedeskChat = runBlocking {
        mutex.withLock {
            setConfiguration(chatConfiguration)
            instanceBox ?: InstanceBoxUsedesk(
                context,
                requireConfiguration(),
                usedeskMessagesRepository
            ).also {
                instanceBox = it
            }
        }
    }.chatInteractor

    @JvmStatic
    fun getInstance(): IUsedeskChat? = instanceBox?.chatInteractor

    @JvmStatic
    fun requireInstance(): IUsedeskChat = getInstance()
        ?: throw RuntimeException("Must call UsedeskChatSdk.init(...) before")

    /**
     * Завершает работу IUsedeskChat
     * При force == false завершит работу только в том случае, если не осталось слушателей
     */
    @JvmStatic
    @JvmOverloads
    fun release(force: Boolean = true) {
        runBlocking {
            mutex.withLock {
                instanceBox?.also {
                    if (force || it.chatInteractor.isNoListeners()) {
                        it.release()
                        instanceBox = null
                    }
                }
            }
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
        this.usedeskMessagesRepository = usedeskMessagesRepository
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