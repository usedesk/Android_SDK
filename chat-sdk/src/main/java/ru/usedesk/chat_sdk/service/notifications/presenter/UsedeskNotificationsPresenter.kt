package ru.usedesk.chat_sdk.service.notifications.presenter

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgent

class UsedeskNotificationsPresenter {
    private var lastModel: UsedeskNotificationsModel? = null

    private lateinit var actionListenerRx: IUsedeskActionListenerRx

    fun init(onModel: (UsedeskNotificationsModel?) -> Unit) {
        UsedeskChatSdk.requireInstance().apply {
            actionListenerRx = object : IUsedeskActionListenerRx() {
                override fun onNewMessageObservable(
                    newMessageObservable: Observable<UsedeskMessage>
                ): Disposable? {
                    return newMessageObservable.filter {
                        it is UsedeskMessageAgent
                    }.map {
                        UsedeskNotificationsModel(it)
                    }.map {
                        val curModel = lastModel
                        lastModel = if (curModel == null) {
                            it
                        } else {
                            UsedeskNotificationsModel(it.message, curModel.count + 1)
                        }
                        lastModel
                    }.subscribe {
                        onModel(it)
                    }
                }
            }

            addActionListener(actionListenerRx)
            connectRx().subscribe()
        }
    }

    fun onClear() {
        UsedeskChatSdk.getInstance()
            ?.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }
}