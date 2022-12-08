package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity

internal sealed class SocketRequest(val type: String) {
    class Init(
        val token: String?,
        val companyId: String,
        val url: String,
        messageLimit: Int?
    ) : SocketRequest(TYPE) {

        private val payload = Payload(messageLimit)

        class Payload(
            val messageLimit: Int?
        ) {
            val sdk = VALUE_CURRENT_SDK
            val type = VALUE_TYPE_SDK
        }

        companion object {
            private const val TYPE = "@@server/chat/INIT"
            private const val VALUE_CURRENT_SDK = "android"
            private const val VALUE_TYPE_SDK = "sdk"
        }
    }

    class SendMessage(
        text: String,
        messageId: Long
    ) : SocketRequest(TYPE) {

        private val message = RequestMessage(text, messageId)

        companion object {
            private const val TYPE = "@@server/chat/SEND_MESSAGE"
        }

        private class RequestMessage(
            private val text: String,
            messageId: Long
        ) {
            private val payload = Payload(messageId)
        }

        private class Payload(
            private val messageId: Long
        )
    }

    class Feedback(
        messageId: Long,
        feedback: String
    ) : SocketRequest(TYPE) {

        private val payload = Payload(feedback, messageId)

        private class Payload(
            private val data: String,
            private val messageId: Long
        ) {
            private val type: String = VALUE_FEEDBACK_ACTION
        }

        companion object {
            private const val TYPE = "@@server/chat/CALLBACK"
            private const val VALUE_FEEDBACK_ACTION = "action"
        }
    }
}