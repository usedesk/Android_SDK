
package ru.usedesk.chat_sdk.entity

data class UsedeskOfflineForm(
    val clientName: String,
    val clientEmail: String,
    val topic: String,
    val fields: List<Field>,
    val message: String
) {
    data class Field(
        val key: String,
        val title: String,
        val value: String
    )
}