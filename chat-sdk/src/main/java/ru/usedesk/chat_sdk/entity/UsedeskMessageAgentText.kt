package ru.usedesk.chat_sdk.entity

import java.util.*

class UsedeskMessageAgentText(
    id: Long,
    createdAt: Calendar,
    text: String,
    convertedText: String,
    val forms: List<Form>,
    val formsLoaded: Boolean,
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

    sealed class Form(
        val id: Long,
        val name: String
    ) {
        class Button(
            id: Long,
            name: String,
            val url: String,
            val type: String
        ) : Form(id, name) {
            companion object {
                const val FORM_APPLY_BUTTON_ID = 0L
            }
        }

        sealed class Field(
            id: Long,
            name: String,
            val required: Boolean
        ) : Form(id, name) {
            class Stub(
                id: Long,
                name: String,
                required: Boolean
            ) : Field(id, name, required)

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

            class List(
                id: Long,
                name: String,
                required: Boolean,
                val items: kotlin.collections.List<Item>
            ) : Field(id, name, required) {
                data class Item(
                    val id: Long,
                    val name: String
                )
            }
        }
    }
}