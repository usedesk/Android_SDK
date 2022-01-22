package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class InstanceBoxUsedesk(
    context: Context,
    chatConfiguration: UsedeskChatConfiguration,
    messagesRepository: IUsedeskMessagesRepository?//TODO:
) {

    private val ioScheduler = Schedulers.io()

    private val daggerChatComponent = DaggerChatComponent.builder()
        .bindAppContext(context.applicationContext)
        .bindChatConfiguration(chatConfiguration)
        .build()

    val chatInteractor = daggerChatComponent.chatInteractor

    fun release() {
        chatInteractor.releaseRx()
            .subscribeOn(ioScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }
}