package ru.usedesk.chat_sdk.entity

data class UsedeskForm(
    val id: Long = 0,
    val fields: List<UsedeskMessageAgentText.Field> = listOf(),
    val state: State = State.NOT_LOADED
) {
    enum class State {
        NOT_LOADED,
        LOADING,
        LOADED,
        SENDING,
        SENT
    }
}