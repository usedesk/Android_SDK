package ru.usedesk.chat_sdk.internal.di

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.internal.appdi.InjectBox
import toothpick.ktp.delegate.inject

class InstanceBox(
        appContext: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration,
        actionListener: IUsedeskActionListener
) : InjectBox() {

    val usedeskChatSdk: IUsedeskChat by inject()

    init {
        init(MainModule(appContext, usedeskChatConfiguration, actionListener))
    }

    override fun release() {
        usedeskChatSdk.disconnectRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        super.release()
    }
}