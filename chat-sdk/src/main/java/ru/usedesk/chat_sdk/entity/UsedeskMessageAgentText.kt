package ru.usedesk.chat_sdk.entity

import java.util.*

data class UsedeskMessageAgentText(
    override val id: Long,
    override val createdAt: Calendar,
    override val text: String,
    override val convertedText: String,
    override val name: String,
    override val avatar: String,
    val feedbackNeeded: Boolean,
    val feedback: UsedeskFeedback?,
    val forms: List<Form>,
    val formsLoaded: Boolean
) : UsedeskMessage.Text, UsedeskMessageOwner.Agent {

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
                val items: kotlin.collections.List<Item>,
                val loaded: Boolean
            ) : Field(id, name, required) {
                data class Item(
                    val id: Long,
                    val name: String
                )
            }
        }
    }
}