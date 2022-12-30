package ru.usedesk.common_sdk.entity

import java.util.concurrent.atomic.AtomicBoolean

class UsedeskSingleLifeEvent<DATA>(
    data: DATA
) : UsedeskEvent<DATA>(data) {
    private val processed = AtomicBoolean(false)

    override fun use(onProcess: (DATA) -> Unit) {
        if (!processed.getAndSet(true)) {
            super.use(onProcess)
        }
    }
}