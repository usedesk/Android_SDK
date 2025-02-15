package ru.usedesk.chat_sdk.domain

import ru.usedesk.chat_sdk.data.repository.InitClientMessageRepository
import ru.usedesk.chat_sdk.data.repository.ToSend
import ru.usedesk.chat_sdk.data.repository.ToSendRepository
import javax.inject.Inject

internal class InitClientMessageInteractor @Inject constructor(
    private val initClientMessageRepository: InitClientMessageRepository,
    private val toSendRepository: ToSendRepository,
) {
    fun sendInitClientMessage() {
        val offlineFormMessage = initClientMessageRepository.offlineFormMessage
        val clientInitMessage = initClientMessageRepository.initClientMessage
        val anyMessage = offlineFormMessage ?: clientInitMessage ?: return
        toSendRepository.addToSend(ToSend.Text(anyMessage, null))
        initClientMessageRepository.clearMessages()
    }
}