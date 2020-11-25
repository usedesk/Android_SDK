package ru.usedesk.chat_sdk.external.entity

open class UsedeskEvent<DATA>(
        open val data: DATA
) {

    open fun process(onProcess: (DATA) -> Unit) {
        onProcess(data)
    }
}