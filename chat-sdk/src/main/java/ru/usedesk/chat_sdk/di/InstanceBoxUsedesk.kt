package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.common_sdk.di.UsedeskInjectBox
import toothpick.ktp.delegate.inject

internal class InstanceBoxUsedesk(
        context: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration
) : UsedeskInjectBox() {

    val usedeskChatSdk: IUsedeskChat by inject()

    init {
        val appContext = context.applicationContext
        init(UsedeskCommonModule(appContext), MainModule(usedeskChatConfiguration))
    }

    override fun release() {
        usedeskChatSdk.disconnectRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        super.release()
    }
}