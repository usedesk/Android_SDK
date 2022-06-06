package ru.usedesk.chat_gui.chat

import io.reactivex.Observable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

internal class ChatViewModel : UsedeskViewModel<ChatViewModel.Model>(Model()) {
    private val usedeskChat = UsedeskChatSdk.requireInstance()

    val goLoadingEvent = UsedeskSingleLifeEvent<Any?>(null)

    private val actionListenerRx = object : IUsedeskActionListenerRx() {
        override fun onOfflineFormExpectedObservable(
            offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
        ) = offlineFormExpectedObservable.observeOn(mainThread).subscribe {
            setModel { model ->
                model.copy(
                    offlineFormSettings = it
                )
            }
        }

        override fun onClientTokenObservable(
            clientTokenObservable: Observable<String>
        ) = clientTokenObservable.observeOn(mainThread).subscribe {
            setModel { model ->
                model.copy(
                    clientToken = it
                )
            }
        }

        override fun onExceptionObservable(
            exceptionObservable: Observable<Exception>
        ) = exceptionObservable.subscribe {
            it.printStackTrace()
        }
    }

    fun init() {
        doInit {
            usedeskChat.addActionListener(actionListenerRx)
        }
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }

    data class Model(
        val clientToken: String? = null,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null
    )
}