
package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity

import com.google.gson.annotations.SerializedName

internal sealed interface SocketResponse {
    class Inited(
        @SerializedName("token") val token: String?,
        @SerializedName("setup") val setup: Setup?
    ) : SocketResponse {

        class Setup(
            @SerializedName("noOperators")
            val noOperators: Boolean?,
            @SerializedName("messages") val messages: List<AddMessage.Message?>?,
            @SerializedName("ticket") val ticket: Ticket?,
            @SerializedName("callback_settings") val callbackSettings: CallbackSettings?
        ) {

            class CallbackSettings(
                @SerializedName("work_type") val workType: String?,
                @SerializedName("callback_title") val callbackTitle: String?,
                @SerializedName("callback_greeting") val callbackGreeting: String?,
                @SerializedName("custom_fields") val customFields: Array<CustomField?>?,
                @SerializedName("topics") val topics: Array<Topic?>?,
                @SerializedName("topics_title") val topicsTitle: String?,
                @SerializedName("topics_required") val topicsRequired: Int?
            ) {

                class CustomField(
                    @SerializedName("type") val type: String?,
                    @SerializedName("placeholder") val placeholder: String?,
                    @SerializedName("checked") val checked: Boolean?,
                    @SerializedName("required") val required: Boolean?
                )

                class Topic(
                    @SerializedName("text") val text: String?,
                    @SerializedName("checked") val checked: Boolean?
                )
            }

            class Ticket(
                @SerializedName("id") val id: Long?,
                @SerializedName("status_id") val statusId: Int?,
                @SerializedName("client_id") val clientId: Long?
            )
        }
    }

    class SetClient(
        @SerializedName("state") val state: State?,
        @SerializedName("reset") val reset: Boolean?
    ) : SocketResponse {

        class State {
            @SerializedName("client") val client: Client? = null

            class Client {
                @SerializedName("token") val token: String? = null
                @SerializedName("email") val email: String? = null
                @SerializedName("chat") val chat: Int? = null
            }
        }
    }

    class AddMessage(@SerializedName("message") val message: Message?) : SocketResponse {

        companion object {
            const val TYPE_OPERATOR_TO_CLIENT = "operator_to_client"
            const val TYPE_CLIENT_TO_OPERATOR = "client_to_operator"
            const val TYPE_CLIENT_TO_BOT = "client_to_bot"
            const val TYPE_BOT_TO_CLIENT = "bot_to_client"
        }

        class Message(
            @SerializedName("payload") val payload: Payload?,
            @SerializedName("id") val id: String?,
            @SerializedName("type") val type: String?,
            @SerializedName("text") val text: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("name") val name: String?,
            @SerializedName("chat") val chat: Any?,
            @SerializedName("file") val file: File?
        ) {

            class File(
                @SerializedName("name") val name: String?,
                @SerializedName("type") val type: String?,
                @SerializedName("content") val content: String?,
                @SerializedName("size") val size: String?,
                @SerializedName("fileId") val fileId: Long?
            )

            class Payload(
                @SerializedName("buttons") val buttons: Array<Button?>?,
                @SerializedName("buttons_csi") val buttonsCsi: ButtonsCsi? = null,
                @SerializedName("userRating")
                val userRating: String? = null,
                @SerializedName("avatar") val avatar: String? = null,
                @SerializedName("message_id") val messageId: String? = null
            ) {

                class Button {
                    @SerializedName("type") val type: String? = null
                    @SerializedName("icon") val icon: String? = null
                    @SerializedName("data") val data: String? = null
                }

                class ButtonsCsi(
                    @SerializedName("button_type") val buttonType: String?,
                    @SerializedName("nps_type") val npsType: String?,
                    @SerializedName("icon_type") val iconType: String?,
                    @SerializedName("data") val data: List<Item?>?
                ) {
                    class Item(
                        @SerializedName("label") val label: String?,
                        @SerializedName("id") val id: String?
                    )
                }
            }
        }
    }

    class FeedbackResponse(@SerializedName("answer") val answer: Answer?) : SocketResponse {
        class Answer(@SerializedName("status") val status: Boolean? = null)
    }

    class ErrorResponse : SocketResponse {
        @SerializedName("message") val message: String? = null
        @SerializedName("code") val code: Int? = null
    }
}
