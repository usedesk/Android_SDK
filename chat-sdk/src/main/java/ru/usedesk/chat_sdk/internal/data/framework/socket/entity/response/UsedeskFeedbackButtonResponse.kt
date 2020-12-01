package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

data class UsedeskFeedbackButtonResponse(
        val type: String?,
        val title: String?,
        val icon: Icon?,
        val data: String?
) {
    enum class Icon {
        LIKE, DISLIKE
    }
}