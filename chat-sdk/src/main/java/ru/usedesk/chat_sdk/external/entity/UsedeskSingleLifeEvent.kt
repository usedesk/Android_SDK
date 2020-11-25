package ru.usedesk.chat_sdk.external.entity

class UsedeskSingleLifeEvent<DATA>(
        data: DATA
) : UsedeskEvent<DATA>(data) {
    private var isProcessed = false

    override fun process(onProcess: (DATA) -> Unit) {
        if (!isProcessed) {
            isProcessed = true
            super.process(onProcess)
        }
    }
}