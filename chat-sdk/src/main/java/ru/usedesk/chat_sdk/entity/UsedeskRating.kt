package ru.usedesk.chat_sdk.entity

data class UsedeskRating(
    val appearance: Appearance,
    val options: List<Option>,
    val selectedId: String? = null
) {
    data class Option @JvmOverloads constructor(
        val id: String,
        val label: String,
        val data: String = id,
        val reaction: Reaction? = null
    ) {
        enum class Reaction {
            LIKE,
            DISLIKE
        }
    }

    enum class Appearance {
        REACTIONS,
        STARS
    }
}
