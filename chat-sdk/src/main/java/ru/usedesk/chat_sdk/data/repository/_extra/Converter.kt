package ru.usedesk.chat_sdk.data.repository._extra

internal interface Converter<FROM, TO> {

    fun convert(from: FROM): TO
}