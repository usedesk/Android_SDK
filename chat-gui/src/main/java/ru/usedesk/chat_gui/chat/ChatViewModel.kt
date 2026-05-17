
package ru.usedesk.chat_gui.chat

import kotlinx.coroutines.launch
import ru.usedesk.chat_gui.chat.di.ChatUiComponent
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.UsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.entity.UsedeskEvent
import javax.inject.Inject

internal class ChatViewModel @Inject constructor(
    private val usedeskChat: UsedeskChat,
    val usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory
) : UsedeskViewModel<ChatViewModel.Model>(Model()) {

    private val actionListener = object : UsedeskActionListener {
        override fun onModel(
            model: UsedeskChat.Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            mainScope.launch {
                setModel {
                    copy(
                        clientToken = model.clientToken,
                        offlineFormSettings = model.offlineFormSettings
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

        ChatUiComponent.close()
    }

    data class Model(
        val clientToken: String? = null,
        val offlineFormSettings: UsedeskOfflineFormSettings? = null,
        val goLoading: UsedeskEvent<Unit> = UsedeskEvent(Unit)
    )
}