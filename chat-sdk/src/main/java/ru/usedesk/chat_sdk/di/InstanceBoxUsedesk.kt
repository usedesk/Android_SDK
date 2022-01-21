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
    usedeskChatConfiguration: UsedeskChatConfiguration,
    usedeskMessagesRepository: IUsedeskMessagesRepository?
) {

    private val ioScheduler = Schedulers.io()

    @Inject
    lateinit var usedeskChatSdk: IUsedeskChat

    init {
        //DaggerChatComponent
        
    }

    fun release() {
        usedeskChatSdk.releaseRx()
            .subscribeOn(ioScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }
}