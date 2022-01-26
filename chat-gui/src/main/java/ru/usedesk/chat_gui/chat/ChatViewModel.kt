package ru.usedesk.chat_gui.chat

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListenerRx
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel

internal class ChatViewModel : UsedeskViewModel() {

    val exceptionLiveData = MutableLiveData<Exception?>()
    val pageLiveData = MutableLiveData<ChatNavigation.Page?>()
    val clientTokenLiveData = MutableLiveData<String?>()

    val configuration = UsedeskChatSdk.requireConfiguration()
    var offlineFormSettings: UsedeskOfflineFormSettings? = null
        private set

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    private lateinit var chatNavigation: ChatNavigation
    private lateinit var actionListenerRx: IUsedeskActionListenerRx

    private var agentName: String? = null
    private var rejectedFileExtensions: Array<String> = arrayOf()
    private var chatInited = false

    fun init(
        chatNavigation: ChatNavigation,
        agentName: String?,
        rejectedFileExtensions: Array<String>,
        chatInited: Boolean
    ) {
        doInit {
            this.chatNavigation = chatNavigation
            this.agentName = agentName
            this.rejectedFileExtensions = rejectedFileExtensions
            this.chatInited = chatInited

            addDisposable(chatNavigation.pageRx().subscribe {
                pageLiveData.value = it
            })

            if (!chatInited) {
                chatNavigation.goLoading()
            }

            actionListenerRx = object : IUsedeskActionListenerRx() {
                override fun onConnectedStateObservable(
                    connectedStateObservable: Observable<Boolean>
                ): Disposable? {
                    return connectedStateObservable.subscribe {
                        if (!it) {
                            doIt(usedeskChat.connectRx())
                        }
                    }
                }

                override fun onClientTokenObservable(
                    clientTokenObservable: Observable<String>
                ): Disposable? {
                    return clientTokenObservable.subscribe {
                        clientTokenLiveData.postValue(it)
                    }
                }

                override fun onMessagesObservable(
                    messagesObservable: Observable<List<UsedeskMessage>>
                ): Disposable? {
                    return messagesObservable.subscribe {
                        if (!this@ChatViewModel.chatInited) {
                            this@ChatViewModel.chatInited = true
                            chatNavigation.goMessages(agentName, rejectedFileExtensions)
                        }
                    }
                }

                override fun onOfflineFormExpectedObservable(
                    offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
                ): Disposable? {
                    return offlineFormExpectedObservable.subscribe {
                        offlineFormSettings = it
                        chatNavigation.goOfflineForm()
                    }
                }

                override fun onExceptionObservable(
                    exceptionObservable: Observable<Exception>
                ): Disposable? {
                    return exceptionObservable.subscribe {
                        exceptionLiveData.postValue(it)
                    }
                }
            }
            usedeskChat.addActionListener(actionListenerRx)
        }
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }

    fun onBackPressed(): Boolean {
        return chatNavigation.onBackPressed()
    }

    fun goOfflineFormSelector(items: Array<String>, selectedIndex: Int) {
        chatNavigation.goOfflineFormSelector(items, selectedIndex)
    }

    fun setSubjectIndex(index: Int) {
        chatNavigation.setSubjectIndex(index)
    }

    fun goMessages() {
        chatNavigation.goMessages(agentName, rejectedFileExtensions)
    }
}