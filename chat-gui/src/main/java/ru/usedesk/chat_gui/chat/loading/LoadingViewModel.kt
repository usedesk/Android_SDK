
package ru.usedesk.chat_gui.chat.loading

import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskConnectionState
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent

internal class LoadingViewModel : UsedeskViewModel<LoadingViewModel.Model>(Model()) {

    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val actionListener = object : IUsedeskActionListener {
        override fun onModel(
            model: IUsedeskChat.Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            mainScope.launch {
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
                            model.offlineFormSettings != null -> UsedeskEvent(Page.OFFLINE_FORM)
                            model.inited -> UsedeskEvent(Page.MESSAGES)
                            else -> null
                        }
                    )
                }
            }
        }
    }

    init {
        usedeskChat.addActionListener(actionListener)
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)

        UsedeskChatSdk.release(false)
    }

    data class Model(
        val state: State = State.LOADING,
        val goNext: UsedeskEvent<Page>? = null
    )

    enum class Page {
        OFFLINE_FORM,
        MESSAGES
    }
}