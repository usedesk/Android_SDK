package ru.usedesk.chat_sdk.external.entity

import com.google.gson.annotations.SerializedName

@Deprecated("Use new data class UsedeskChatItem")
class UsedeskPayload {
    @SerializedName(KEY_TICKET_ID)
    val ticketId: Long = 0

    @SerializedName(KEY_FEEDBACK_BUTTONS)
    val feedbackButtons: List<UsedeskFeedbackButton>? = null
    val isCsi = false

    private val userRating: String? = null

    val avatar: String? = null

    fun hasFeedback(): Boolean {
        return feedbackButtons != null && (userRating == null || userRating.isEmpty())
    }

    companion object {
        private const val KEY_TICKET_ID = "ticket_id"
        private const val KEY_FEEDBACK_BUTTONS = "buttons"
    }
}