
package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity

import com.google.gson.annotations.SerializedName

internal sealed interface SocketResponse {
    class Inited(
        val token: String?,
        val setup: Setup?
    ) : SocketResponse {

        class Setup(
            @SerializedName("noOperators")
            val noOperators: Boolean?,
            val messages: List<AddMessage.Message?>?,
            val ticket: Ticket?,
            val callbackSettings: CallbackSettings?
        ) {

            class CallbackSettings(
                val workType: String?,
                val callbackTitle: String?,
                val callbackGreeting: String?,
                val customFields: Array<CustomField?>?,
                val topics: Array<Topic?>?,
                val topicsTitle: String?,
                val topicsRequired: Int?
            ) {

                class CustomField(
                    val type: String?,
                    val placeholder: String?,
                    val checked: Boolean?,
                    val required: Boolean?
                )

                class Topic(
                    val text: String?,
                    val checked: Boolean?
                )
            }

            class Ticket(
                val id: Long?,
                val statusId: Int?
            )
        }
    }

    class SetClient(
        val state: State?,
        val reset: Boolean?
    ) : SocketResponse {

        class State {
            val client: Client? = null

            class Client {
                val token: String? = null
                val email: String? = null
                val chat: Int? = null
            }
        }
    }

    class AddMessage(val message: Message?) : SocketResponse {

        companion object {
            const val TYPE_OPERATOR_TO_CLIENT = "operator_to_client"
            const val TYPE_CLIENT_TO_OPERATOR = "client_to_operator"
            const val TYPE_CLIENT_TO_BOT = "client_to_bot"
            const val TYPE_BOT_TO_CLIENT = "bot_to_client"
        }

        class Message(
            val payload: Payload?,
            val id: Long?,
            val type: String?,
            val text: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            val name: String?,
            val chat: Any?,
            val file: File?
        ) {

            class File(
                val name: String?,
                val type: String?,
                val content: String?,
                val size: String?
            )

            class Payload(val buttons: Array<Button?>?) {

                @SerializedName("userRating")
                val userRating: String? = null
                val avatar: String? = null
                val messageId: Long? = null

                class Button {
                    val type: String? = null
                    val icon: String? = null
                    val data: String? = null
                }
            }
        }
    }

    class FeedbackResponse(val answer: Answer?) : SocketResponse {
        class Answer(val status: Boolean? = null)
    }

    class ErrorResponse : SocketResponse {
        val message: String? = null
        val code: Int? = null
    }
}