package ru.usedesk.common_sdk.entity

open class UsedeskEvent<DATA>(
        open val data: DATA
) {

    open fun process(onProcess: (DATA) -> Unit) {
        onProcess(data)
    }
}