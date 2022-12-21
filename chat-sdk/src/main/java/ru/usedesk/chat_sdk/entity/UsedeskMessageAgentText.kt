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
    val buttons: List<Button>,
    val hasForm: Boolean
) : UsedeskMessage.Text, UsedeskMessageOwner.Agent {

    data class Button(
        val name: String = "",
        val url: String = "",
        val type: String = ""
    )

    sealed interface Field {
        val id: String
        val name: String
        val required: Boolean
        val hasError: Boolean

        data class Text(
            override val id: String,
            override val name: String,
            override val required: Boolean,
            override val hasError: Boolean = false,
            val type: Type,
            val text: String = ""
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
            override val id: String,
            override val name: String,
            override val required: Boolean,
            override val hasError: Boolean = false,
            val checked: Boolean = false
        ) : Field

        data class List(
            override val id: String,
            override val name: String,
            override val required: Boolean,
            override val hasError: Boolean = false,
            val parentId: String? = null,
            val items: kotlin.collections.List<Item> = listOf(),
            val selected: Item? = null
        ) : Field {
            data class Item(
                val id: Long,
                val name: String,
                val parentValueId: Long?
            )
        }
    }
}