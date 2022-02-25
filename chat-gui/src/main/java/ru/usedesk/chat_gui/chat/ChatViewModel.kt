package ru.usedesk.chat_gui.chat

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel

internal class ChatViewModel : UsedeskViewModel<ChatViewModel.Model>(Model()) {
    private val usedeskChat = UsedeskChatSdk.requireInstance()

    var agentName: String? = null
        private set
    var rejectedFileExtensions: Set<String> = setOf()
        private set
    var messagesDateFormat: String = ""
        private set
    var messageTimeFormat: String = ""
        private set


    private val actionListenerRx = object : IUsedeskActionListenerRx() {
        override fun onOfflineFormExpectedObservable(
            offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
        ): Disposable? {
            return offlineFormExpectedObservable.observeOn(
                mainThread
            ).subscribe {
                setModel { model ->
                    model.copy(
                        offlineFormSettings = it
                    )
                }
            }
        }

        override fun onClientTokenObservable(
            clientTokenObservable: Observable<String>
        ): Disposable? {
            return clientTokenObservable.observeOn(
                mainThread
            ).subscribe {
                setModel { model ->
                    model.copy(
                        clientToken = it
                    )
                }
            }
        }

        override fun onExceptionObservable(
            exceptionObservable: Observable<Exception>
        ): Disposable? {
            return exceptionObservable.subscribe {
                it.printStackTrace()
            }
        }
    }

    fun init(
        agentName: String?,
        rejectedFileExtensions: Set<String>,
        messagesDateFormat: String,
        messageTimeFormat: String
    ) {
        doInit {
            this.agentName = agentName
            this.rejectedFileExtensions = rejectedFileExtensions
            this.messagesDateFormat = messagesDateFormat
            this.messageTimeFormat = messageTimeFormat

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