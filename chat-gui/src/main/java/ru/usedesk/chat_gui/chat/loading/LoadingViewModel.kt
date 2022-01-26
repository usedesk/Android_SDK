package ru.usedesk.chat_gui.chat.loading

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException

internal class LoadingViewModel : UsedeskViewModel() {

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()
    private val mainScheduler = AndroidSchedulers.mainThread()
    private val actionListener: IUsedeskActionListenerRx

    val modelLiveData = UsedeskLiveData(Model())

    init {
        actionListener = object : IUsedeskActionListenerRx() {
            override fun onConnectedStateObservable(
                connectedStateObservable: Observable<Boolean>
            ): Disposable? {
                return connectedStateObservable.observeOn(mainScheduler).subscribe { connected ->
                    if (connected) {
                        modelLiveData.value = modelLiveData.value.copy(
                            state = State.NONE
                        )
                    }
                }
            }

            override fun onExceptionObservable(exceptionObservable: Observable<Exception>): Disposable? {
                return exceptionObservable.observeOn(mainScheduler).subscribe { usedeskException ->

                    if (usedeskException is UsedeskSocketException &&
                        usedeskException.error == UsedeskSocketException.Error.DISCONNECTED
                    ) {
                        modelLiveData.value = modelLiveData.value.copy(
                            state = State.NO_INTERNET
                        )
                    }
                }
            }
        }
        usedeskChat.addActionListener(actionListener)
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)
    }

    data class Model(
        val state: State = State.LOADING
    )

    enum class State {
        LOADING,
        NO_INTERNET,
        NONE
    }
}