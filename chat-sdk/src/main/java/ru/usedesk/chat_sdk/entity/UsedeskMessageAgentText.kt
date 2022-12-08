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

    sealed interface Form {
        val id: Long
        val name: String

        data class Button(
            override val id: Long = FORM_APPLY_BUTTON_ID,
            override val name: String = "",
            val url: String = "",
            val type: String = ""
        ) : Form {
            companion object {
                const val FORM_APPLY_BUTTON_ID = 0L
            }
        }

        sealed interface Field : Form {
            val required: Boolean

            data class Text(
                override val id: Long,
                override val name: String,
                override val required: Boolean,
                val type: Type
            ) : Field {
                enum class Type {
                    EMAIL,
                    PHONE,
                    NAME,
                    NOTE,
                    POSITION
                }
            }

            data class CheckBox(
                override val id: Long,
                override val name: String,
                override val required: Boolean
            ) : Field

            data class List(
                override val id: Long,
                override val name: String,
                override val required: Boolean,
                val items: kotlin.collections.List<Item>,
                val parentId: Long?
            ) : Field {
                data class Item(
                    val id: Long,
                    val name: String,
                    val parentValueId: Long?
                )
            }
        }
    }
}