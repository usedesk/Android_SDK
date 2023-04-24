
package ru.usedesk.chat_sdk.entity

data class UsedeskForm(
    val id: Long,
    val fields: List<Field> = listOf(),
    val state: State = State.NOT_LOADED
) {
    enum class State {
        NOT_LOADED,
        LOADING,
        LOADING_FAILED,
        LOADED,
        SENDING,
        SENDING_FAILED,
        SENT
    }

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
                NONE,
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
                val parentItemsId: kotlin.collections.List<Long>
            )
        }
    }
}