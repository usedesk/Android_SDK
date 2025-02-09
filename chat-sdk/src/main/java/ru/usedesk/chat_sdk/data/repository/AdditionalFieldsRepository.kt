package ru.usedesk.chat_sdk.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.usedesk.chat_sdk.di.chat.ChatScope
import javax.inject.Inject

@ChatScope
class AdditionalFieldsRepository @Inject constructor() {
    private val _firstMessageSentFlow = MutableStateFlow(false)
    val firstMessageSentFlow: StateFlow<Boolean> = _firstMessageSentFlow.asStateFlow()

    private val _additionalFieldsNeededFlow = MutableStateFlow(true)
    val additionalFieldsNeededFlow: StateFlow<Boolean> = _additionalFieldsNeededFlow.asStateFlow()

    fun messageSent() {
        _firstMessageSentFlow.value = true
    }

    fun additionalFieldsSent(sent: Boolean) {
        _additionalFieldsNeededFlow.value = !sent
    }
}