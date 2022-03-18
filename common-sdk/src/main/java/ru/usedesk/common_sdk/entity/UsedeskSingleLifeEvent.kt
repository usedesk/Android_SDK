package ru.usedesk.common_sdk.entity

class UsedeskSingleLifeEvent<DATA>(
    data: DATA
) : UsedeskEvent<DATA>(data) {
    private var processed = false

    override fun process(onProcess: (DATA) -> Unit) {
        if (!processed) {
            processed = true
            super.process(onProcess)
        }
    }
}