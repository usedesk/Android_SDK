package ru.usedesk.chat_gui.chat.loading

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskConnectionState
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

internal class LoadingViewModel : UsedeskViewModel<LoadingViewModel.Model>(Model()) {

    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val actionListener = object : IUsedeskActionListener {
        override fun onConnectionState(connectionState: UsedeskConnectionState) {
            doMain {
                setModel {
                    copy(
                        state = when (connectionState) {
                            UsedeskConnectionState.DISCONNECTED,
                            UsedeskConnectionState.RECONNECTING -> State.FAILED
                            UsedeskConnectionState.CONNECTING -> State.LOADING
                            UsedeskConnectionState.CONNECTED -> State.LOADING
                        }
                    )
                }
            }
        }

        override fun onOfflineFormExpected(offlineFormSettings: UsedeskOfflineFormSettings) {
            doMain {
                setModel { copy(goNext = UsedeskSingleLifeEvent(Page.OFFLINE_FORM)) }
            }
        }

        override fun onMessagesReceived(messages: List<UsedeskMessage>) {
            doMain {
                setModel { copy(goNext = UsedeskSingleLifeEvent(Page.MESSAGES)) }
            }
        }
    }

    init {
        usedeskChat.addActionListener(actionListener)
        doIo { (usedeskChat.connect()) }
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)

        UsedeskChatSdk.release(false)
    }

    data class Model(
        val state: State = State.LOADING,
        val goNext: UsedeskSingleLifeEvent<Page>? = null
    )

    enum class Page {
        OFFLINE_FORM,
        MESSAGES
    }
}