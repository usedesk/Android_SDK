package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

import android.text.TextUtils
import ru.usedesk.chat_sdk.external.entity.UsedeskClient
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.PayloadMessage
import java.util.*

class Setup {
    val waitingEmail = true
    val isNoOperators = false
    val client: UsedeskClient? = null

    private val messages: List<PayloadMessage>? = null

    fun getMessages(): List<UsedeskMessage> {
        if (messages == null) {
            return ArrayList()
        }
        val filteredMessages: MutableList<UsedeskMessage> = ArrayList(messages.size)
        for (payloadMessage in messages) {
            if (payloadMessage.chat != null
                    && (!TextUtils.isEmpty(payloadMessage.text) || payloadMessage.file != null)) {
                filteredMessages.add(UsedeskMessage(payloadMessage, payloadMessage.payload, null))
            }
        }
        return filteredMessages
    }
}