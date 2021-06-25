package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.common_sdk.di.UsedeskInjectBox
import toothpick.ktp.delegate.inject
import javax.inject.Named

internal class InstanceBoxUsedesk(
    context: Context,
    usedeskChatConfiguration: UsedeskChatConfiguration
) : UsedeskInjectBox() {

    val usedeskChatSdk: IUsedeskChat by inject()

    val ioScheduler: Scheduler by inject()

    init {
        val appContext = context.applicationContext
        init(UsedeskCommonModule(appContext), MainModule(usedeskChatConfiguration))
    }

    override fun release() {
        usedeskChatSdk.releaseRx()
            .subscribeOn(ioScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        super.release()
    }
}