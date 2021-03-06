package ru.usedesk.chat_sdk.data.repository._extra

import java.util.*

internal abstract class Factory<K, T> {
    private val instanceMap: MutableMap<K, T?> = HashMap()

    fun getInstance(key: K): T {
        return instanceMap[key] ?: createInstance(key).also {
            instanceMap[key] = it
        }
    }

    protected abstract fun createInstance(key: K): T
}