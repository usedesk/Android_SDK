package ru.usedesk.chat_sdk.data.repository._extra

internal abstract class Converter<FROM, TO> {

    abstract fun convert(from: FROM): TO

    protected fun <T> convertOrNull(onConvert: () -> T?): T? = try {
        onConvert()
    } catch (e: Exception) {
        null
    }
}