package ru.usedesk.chat_sdk.external.service.notifications.presenter

import android.annotation.SuppressLint
import io.reactivex.Observable
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.getInstance
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageAgent
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class UsedeskNotificationsPresenter {
    private var model: UsedeskNotificationsModel? = null
    private val actionListenerRx = UsedeskActionListenerRx()

    val actionListener: IUsedeskActionListener = actionListenerRx

    private val newModelObservable: Observable<UsedeskNotificationsModel> = actionListenerRx.newChatItemObservable.filter {
        it is UsedeskMessageAgent
    }.map {
        UsedeskNotificationsModel(it)
    }

    val modelObservable = newModelObservable.map {
        val model = this.model
        this.model = if (model == null) {
            it
        } else {
            reduce(model, it)
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