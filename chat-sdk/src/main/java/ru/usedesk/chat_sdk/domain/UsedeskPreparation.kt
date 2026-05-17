package ru.usedesk.chat_sdk.domain

interface UsedeskPreparation {
    fun createChat(
        apiToken: String,
        onResult: (CreateChatResult) -> Unit
    )

    sealed interface CreateChatResult {
        class Done(val clientToken: String) : CreateChatResult
        object Error : CreateChatResult
    }
}

@Deprecated(
    message = "Use ru.usedesk.chat_sdk.domain.UsedeskPreparation",
    replaceWith = ReplaceWith(
        "UsedeskPreparation",
        "ru.usedesk.chat_sdk.domain.UsedeskPreparation"
    )
)
typealias IUsedeskPreparation = UsedeskPreparation
