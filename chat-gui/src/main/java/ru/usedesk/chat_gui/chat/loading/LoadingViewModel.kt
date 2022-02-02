package ru.usedesk.chat_gui.chat.loading

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskConnectionState
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

internal class LoadingViewModel : UsedeskViewModel<LoadingViewModel.Model>(Model()) {

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()
    private val mainScheduler = AndroidSchedulers.mainThread()
    private val actionListener: IUsedeskActionListenerRx

    init {
        actionListener = object : IUsedeskActionListenerRx() {
            override fun onConnectionStateObservable(
                connectionStateObservable: Observable<UsedeskConnectionState>
            ): Disposable? {
                return connectionStateObservable.observeOn(mainScheduler).subscribe {
                    if (it == UsedeskConnectionState.DISCONNECTED) {
                        setModel { model ->
                            model.copy(
                                state = State.NO_INTERNET
                            )
                        }
                    }
                }
            }

            override fun onOfflineFormExpectedObservable(
                offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
            ): Disposable? {
                return offlineFormExpectedObservable.observeOn(mainScheduler).subscribe {
                    setModel { model ->
                        model.copy(
                            goNext = UsedeskSingleLifeEvent(Page.OFFLINE_FORM)
                        )
                    }
                }
            }

            override fun onMessagesObservable(
                messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.observeOn(mainScheduler).subscribe {
                    setModel { model ->
                        model.copy(
                            goNext = UsedeskSingleLifeEvent(Page.MESSAGES)
                        )
                    }
                }
            }
        }
        usedeskChat.addActionListener(actionListener)
        doIt(usedeskChat.connectRx())
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)

        UsedeskChatSdk.release(false)
    }

    data class Model(
        val state: State = State.LOADING,
        val goNext: UsedeskSingleLifeEvent<Page?> = UsedeskSingleLifeEvent(null)
    )

    enum class State {
        LOADING,
        NO_INTERNET
    }

    enum class Page {
        OFFLINE_FORM,
        MESSAGES
    }
}