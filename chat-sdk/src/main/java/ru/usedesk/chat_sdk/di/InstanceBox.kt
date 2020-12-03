package ru.usedesk.chat_sdk.di

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.InjectBox
import toothpick.ktp.delegate.inject

internal class InstanceBox(
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