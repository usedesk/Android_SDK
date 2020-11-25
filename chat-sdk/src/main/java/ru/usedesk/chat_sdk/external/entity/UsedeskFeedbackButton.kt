package ru.usedesk.chat_sdk.external.entity

data class UsedeskFeedbackButton(
        val type: String,
        val title: String,
        val icon: Icon,
        val data: String
) {
    enum class Icon {
        LIKE, DISLIKE
    }
}