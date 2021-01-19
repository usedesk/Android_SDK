package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.CommonModule
import ru.usedesk.common_sdk.di.UsedeskInjectBox
import toothpick.ktp.delegate.inject

internal class InstanceBoxUsedesk(
        appContext: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration
) : UsedeskInjectBox() {

    val usedeskChatSdk: IUsedeskChat by inject()

    init {
        init(CommonModule(appContext), MainModule(usedeskChatConfiguration))
    }

    override fun release() {
        usedeskChatSdk.disconnectRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        super.release()
    }
}