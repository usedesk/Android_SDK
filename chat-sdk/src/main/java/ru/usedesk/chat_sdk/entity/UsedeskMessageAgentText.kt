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
                val name: String,
                val type: Type,
                val text: String,
                required: Boolean
            ) : Field(required) {
                enum class Type {
                    EMAIL,
                    PHONE,
                    NAME,
                    NOTE,
                    POSITION
                }
            }

            class List(
                val name: String,
                val id: Long,
                val selectedId: Long, //TODO: может сюда лучше лист сразу
                required: Boolean
            ) : Field(required)

            class CheckBox(
                val name: String,
                val checked: Boolean,
                required: Boolean
            ) : Field(required)
        }
    }
}