package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra

internal abstract class BaseRequest(
    val type: String,
    val token: String?
)