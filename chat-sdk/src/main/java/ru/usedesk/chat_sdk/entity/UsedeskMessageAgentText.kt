package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageAgentText(
    id: Long,
    createdAt: Calendar,
    text: String,
    convertedText: String,
    val items: List<Item>,
    val feedbackNeeded: Boolean,
    val feedback: UsedeskFeedback?,
    override val name: String,
    override val avatar: String
) : UsedeskMessageText(
    id,
    createdAt,
    text,
    convertedText
), UsedeskMessageAgent {

    sealed class Item(
        val id: Long,
        val name: String
    ) {
        class Button(
            id: Long,
            name: String,
            val url: String,
            val type: String,
            val isShow: Boolean
        ) : Item(id, name)

        sealed class Field(
            id: Long,
            name: String,
            val required: Boolean
        ) : Item(id, name) {
            class Text(
                id: Long,
                name: String,
                required: Boolean,
                val type: Type
            ) : Field(id, name, required) {
                enum class Type {
                    EMAIL,
                    PHONE,
                    NAME,
                    NOTE,
                    POSITION
                }
            }

            class CheckBox(
                id: Long,
                name: String,
                required: Boolean
            ) : Field(id, name, required)

            open class ItemList(
                id: Long,
                name: String,
                required: Boolean,
                val multiselect: Boolean = false,
                val items: List<ListItem> = listOf()
            ) : Field(id, name, required) {
                data class ListItem(
                    val id: Long,
                    val name: String,
                    val selected: Boolean
                )
            }
        }
    }
}