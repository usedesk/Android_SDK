package ru.usedesk.chat_sdk.service.notifications.presenter

import android.annotation.SuppressLint
import io.reactivex.Observable
import ru.usedesk.chat_sdk.UsedeskChatSdk.getInstance
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgent
import java.util.concurrent.TimeUnit

class UsedeskNotificationsPresenter {
    private var model: UsedeskNotificationsModel? = null
    private val actionListenerRx = UsedeskActionListenerRx()

    val actionListener: IUsedeskActionListener = actionListenerRx

    private val newModelObservable: Observable<UsedeskNotificationsModel> = actionListenerRx.newMessageObservable.filter {
        it is UsedeskMessageAgent
    }.map {
        UsedeskNotificationsModel(it)
    }

    val modelObservable = newModelObservable.map {
        val curModel = this.model
        model = if (curModel == null) {
            it
        } else {
            reduce(curModel, it)
        }
        model
    }

    private fun reduce(previousModel: UsedeskNotificationsModel,
                       newModel: UsedeskNotificationsModel): UsedeskNotificationsModel {
        return UsedeskNotificationsModel(previousModel.message, previousModel.count + 1)
    }

    @SuppressLint("CheckResult")
    fun init() {
        actionListenerRx.disconnectedObservable.delay(5, TimeUnit.SECONDS).subscribe {
            connect()
        }
        connect()
    }

    private fun connect() {
        getInstance().connectRx().subscribe()
    }
}