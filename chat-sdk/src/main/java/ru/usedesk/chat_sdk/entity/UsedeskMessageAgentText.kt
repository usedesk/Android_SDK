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

    sealed interface Item {
        class Button(
            val text: String,
            val url: String,
            val type: String,
            val isShow: Boolean
        ) : Item

        sealed class Field(val required: Boolean) : Item {
            class Text(
                required: Boolean,
                val name: String,
                val type: Type,
                val text: String = ""
            ) : Field(required) {
                enum class Type {
                    EMAIL,
                    PHONE,
                    NAME,
                    NOTE,
                    POSITION
                }
            }

            class CheckBox(
                required: Boolean,
                val name: String,
                val checked: Boolean = false
            ) : Field(required)

            open class ItemList(
                required: Boolean,
                val name: String,
                val id: Long
            ) : Field(required)

            class LoadedItemList(
                required: Boolean,
                name: String,
                id: Long,
                val multiselect: Boolean,
                val items: List<ListItem>
            ) : ItemList(
                required,
                name,
                id
            ) {
                data class ListItem(
                    val id: Long,
                    val name: String,
                    val selected: Boolean
                )
            }
        }
    }
}