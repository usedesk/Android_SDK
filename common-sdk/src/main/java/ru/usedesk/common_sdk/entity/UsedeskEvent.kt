
package ru.usedesk.common_sdk.entity

import java.util.concurrent.atomic.AtomicBoolean

class UsedeskEvent<DATA>(val data: DATA) {
    private val processed = AtomicBoolean(false)

    fun use(onProcess: (DATA) -> Unit) {
        if (!processed.getAndSet(true)) {
            onProcess(data)
        }
    }

    suspend fun useSuspend(onProcess: suspend (DATA) -> Unit) {
        if (!processed.getAndSet(true)) {
            onProcess(data)
        }
    }
}