package ru.usedesk.chat_sdk.external.entity

import android.text.Html
import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage

@Deprecated("Use new data class UsedeskChatItem")
class UsedeskMessage(
        baseMessage: BaseMessage,
        val usedeskPayload: UsedeskPayload?,
        val stringPayload: String?
) : BaseMessage(baseMessage) {

    val messageButtons: UsedeskMessageButtons

    init {
        val text = baseMessage.text ?: ""
        messageButtons = UsedeskMessageButtons(Html.fromHtml(Html.fromHtml(text).toString()).toString())
    }

    override val text: String = messageButtons.messageText
}