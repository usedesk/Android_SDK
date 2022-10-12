package ru.usedesk.chat_sdk.entity

class UsedeskMessageField(
    val name: String,
    val associate: Associate,
    val required: Boolean
) {
    sealed interface Associate {
        object Email : Associate
        object Phone : Associate
        object Name : Associate
        object Note : Associate
        object Position : Associate
        class Id(val id: Long) : Associate
    }
}