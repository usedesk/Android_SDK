package ru.usedesk.chat_gui.chat.data.thumbnail

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.chat_sdk.entity.UsedeskMessage

internal interface IThumbnailRepository {
    val thumbnailMapFlow: StateFlow<Map<Long, Uri>>

    fun loadThumbnail(message: UsedeskMessage.File)
}