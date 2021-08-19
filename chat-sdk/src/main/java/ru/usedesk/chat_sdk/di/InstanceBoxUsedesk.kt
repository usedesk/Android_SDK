package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.common_sdk.di.UsedeskInjectBox
import toothpick.ktp.delegate.inject

internal class InstanceBoxUsedesk(
    context: Context,
    usedeskChatConfiguration: UsedeskChatConfiguration,
    usedeskMessagesRepository: IUsedeskMessagesRepository?,
    cacheMessagesWithFile: Boolean
) : UsedeskInjectBox() {

    val usedeskChatSdk: IUsedeskChat by inject()

    private val ioScheduler: Scheduler by inject()

    init {
        val appContext = context.applicationContext
        init(
            UsedeskCommonModule(appContext),
            MainModule(
                usedeskChatConfiguration,
                usedeskMessagesRepository,
                cacheMessagesWithFile
            )
        )
    }

    override fun release() {
        usedeskChatSdk.releaseRx()
            .subscribeOn(ioScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        super.release()
    }
}