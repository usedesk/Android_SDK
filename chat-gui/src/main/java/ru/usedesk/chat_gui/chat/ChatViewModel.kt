package ru.usedesk.chat_gui.chat

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

internal class ChatViewModel : UsedeskViewModel<ChatViewModel.Model>(Model()) {
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
                        clientToken = clientToken,
                        offlineFormSettings = offlineFormSettings
                    )
                }
            }
        }

        override fun onException(usedeskException: Exception) = usedeskException.printStackTrace()
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
        val clientToken: String? = null,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val goLoading: UsedeskSingleLifeEvent<Unit> = UsedeskSingleLifeEvent(Unit)
    )
}