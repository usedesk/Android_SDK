package ru.usedesk.chat_sdk.data

internal abstract class Converter<FROM, TO> {

    abstract fun convert(from: FROM): TO

    protected fun <T> convertOrNull(onConvert: () -> T?): T? {
        return try {
            onConvert()
        } catch (e: Exception) {
            null
        }
    }
}