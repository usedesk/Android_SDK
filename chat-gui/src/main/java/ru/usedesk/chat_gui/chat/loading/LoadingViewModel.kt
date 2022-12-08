package ru.usedesk.chat_gui.chat.loading

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskConnectionState
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

internal class LoadingViewModel : UsedeskViewModel<LoadingViewModel.Model>(Model()) {

    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val actionListener = object : IUsedeskActionListener {
        override fun onModel(
            model: IUsedeskChat.Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            doMain {
                setModel {
                    copy(
                        state = when (model.connectionState) {
                            UsedeskConnectionState.DISCONNECTED -> State.FAILED
                            UsedeskConnectionState.RECONNECTING -> State.RELOADING
                            UsedeskConnectionState.NONE,
                            UsedeskConnectionState.CONNECTING,
                            UsedeskConnectionState.CONNECTED -> State.LOADING
                        },
                        goNext = when {
                            model.offlineFormSettings != null -> UsedeskSingleLifeEvent(Page.OFFLINE_FORM)
                            model.inited -> UsedeskSingleLifeEvent(Page.MESSAGES)
                            else -> null
                        }
                    )
                }
            }
        }
    }

    init {
        usedeskChat.addActionListener(actionListener)
        usedeskChat.connect()
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