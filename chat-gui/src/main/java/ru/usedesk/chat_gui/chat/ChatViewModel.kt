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
            rejectedFileExtensions: Array<String>
    ) {
        this.chatNavigation = chatNavigation
        this.agentName = agentName
        this.rejectedFileExtensions = rejectedFileExtensions

        addDisposable(chatNavigation.pageRx().subscribe {
            pageLiveData.value = it
        })

        chatNavigation.goLoading()

        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onConnectedStateObservable(
                    connectedStateObservable: Observable<Boolean>
            ): Disposable? {
                return connectedStateObservable.subscribe {
                    if (!it) {
                        //doIt(usedeskChat.connectRx())
                    }
                }
            }

            override fun onMessagesObservable(
                    messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.subscribe {
                    if (!chatInited) {
                        chatInited = true
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

    override fun onCleared() {
        super.onCleared()

        UsedeskChatSdk.getInstance()
                ?.removeActionListener(actionListenerRx)

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