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

    val exceptionLiveData = MutableLiveData<Exception>()
    val offlineFormSettingsLiveData = MutableLiveData<UsedeskOfflineFormSettings>()
    val pageLiveData = MutableLiveData<ChatNavigation.Page>()

    val configuration = UsedeskChatSdk.requireConfiguration()

    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    private lateinit var chatNavigation: ChatNavigation
    private lateinit var actionListenerRx: IUsedeskActionListenerRx

    private var agentName: String? = null

    fun init(chatNavigation: ChatNavigation, agentName: String?) {
        this.chatNavigation = chatNavigation
        this.agentName = agentName

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
                        doIt(usedeskChat.connectRx())
                    }
                }
            }

            override fun onMessagesObservable(
                    messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.subscribe {
                    chatNavigation.goMessages(agentName)
                }
            }

            override fun onOfflineFormExpectedObservable(
                    offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>
            ): Disposable? {
                return offlineFormExpectedObservable.subscribe {
                    offlineFormSettingsLiveData.postValue(it)
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
}