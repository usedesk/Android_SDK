package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback

class SendFeedbackRequest(
        token: String,
        feedback: UsedeskFeedback
) : BaseRequest(TYPE, token) {

    private val payload: Payload

    private class Payload internal constructor(feedback: UsedeskFeedback) {
        private val type: String = VALUE_FEEDBACK_ACTION

        @SerializedName(KEY_DATA)
        private val feedback: UsedeskFeedback = feedback
    }

    companion object {
        private const val TYPE = "@@server/chat/CALLBACK"
        private const val KEY_DATA = "data"
        private const val VALUE_FEEDBACK_ACTION = "action"
    }

    init {
        payload = Payload(feedback)
    }
}