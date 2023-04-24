
package ru.usedesk.chat_sdk.domain

interface IUsedeskPreparation {
    fun createChat(
        apiToken: String,
        onResult: (CreateChatResult) -> Unit
    )

    sealed interface CreateChatResult {
        class Done(val clientToken: String) : CreateChatResult
        object Error : CreateChatResult
    }
}