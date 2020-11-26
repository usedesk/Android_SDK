package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile

class RequestMessage {
    val text: String?
    private val usedeskFile: UsedeskFile?

    constructor(text: String) {
        this.text = text
        usedeskFile = null
    }

    constructor(usedeskFile: UsedeskFile) {
        text = null
        this.usedeskFile = usedeskFile
    }
}