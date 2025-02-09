package ru.usedesk.chat_sdk.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.usedesk.chat_sdk.di.chat.ChatScope
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientText
import javax.inject.Inject

@ChatScope
internal class ToSendRepository @Inject constructor() {
    private val _toSendFlow = MutableSharedFlow<ToSend>(extraBufferCapacity = 128)
    val toSendFlow: SharedFlow<ToSend> = _toSendFlow.asSharedFlow()

    private val _toSendTextFlow = MutableSharedFlow<UsedeskMessageClientText>()
    val toSendTextFlow: SharedFlow<UsedeskMessageClientText> = _toSendTextFlow.asSharedFlow()

    private val _toSendFileFlow = MutableSharedFlow<UsedeskMessage.File>()
    val toSendFileFlow: SharedFlow<UsedeskMessage.File> = _toSendFileFlow.asSharedFlow()

    fun addToSend(toSend: ToSend) {
        _toSendFlow.tryEmit(toSend)
    }

    suspend fun toSendText(text: UsedeskMessageClientText) {
        _toSendTextFlow.emit(text)
    }

    suspend fun toSendFile(text: UsedeskMessage.File) {
        _toSendFileFlow.emit(text)
    }
}

internal sealed interface ToSend {
    val localId: String?

    class Text(
        val text: String,
        override val localId: String?
    ) : ToSend

    class File(
        val file: UsedeskFileInfo,
        override val localId: String?
    ) : ToSend
}