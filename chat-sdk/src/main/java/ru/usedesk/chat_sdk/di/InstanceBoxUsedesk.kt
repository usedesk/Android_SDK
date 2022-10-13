package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.UsedeskCustom

internal class InstanceBoxUsedesk(
    context: Context,
    chatConfiguration: UsedeskChatConfiguration,
    messagesRepository: IUsedeskMessagesRepository?
) {
    private val ioScheduler = Schedulers.io()

    private var daggerChatComponent: ChatComponent?

    val chatInteractor: IUsedeskChat

    init {
        val daggerChatComponent = DaggerChatComponent.builder()
            .appContext(context.applicationContext)
            .configuration(chatConfiguration)
            .customMessagesRepository(UsedeskCustom(messagesRepository))
            .build()

        this.daggerChatComponent = daggerChatComponent
        this.chatInteractor = daggerChatComponent.chatInteractor
    }

    fun release() {
        chatInteractor.releaseRx()
            .subscribeOn(ioScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

        daggerChatComponent = null
    }
}