package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.init

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.BaseResponse
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.UsedeskFeedbackButtonResponse
import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage

class InitChatResponse(
        type: String?,
        val token: String?,
        val setup: Setup?,
        val noOperators: Boolean?
) : BaseResponse(type) {

    companion object {
        const val TYPE = "@@chat/current/INITED"
    }

    class Setup(
            val waitingEmail: Boolean?,
            val isNoOperators: Boolean?,
            val messages: List<Message?>?
    ) {

        class Message(
                val payload: Payload?
        ) : BaseMessage() {

            class Payload(
                    val type: Type?,
                    val buttons: List<UsedeskFeedbackButtonResponse?>?,
                    val isCsi: Boolean?,
                    val userRating: String?,
                    val avatar: String? = null,
                    val file: File?,
                    val text: String?
            ) {

                class File(
                        val name: String?,
                        val type: String?,
                        val content: String?,
                        val size: String?
                )

                enum class Type {
                    @SerializedName("operator_to_client")
                    OPERATOR_TO_CLIENT,

                    @SerializedName("client_to_operator")
                    CLIENT_TO_OPERATOR,

                    @SerializedName("client_to_bot")
                    CLIENT_TO_BOT,

                    @SerializedName("bot_to_client")
                    BOT_TO_CLIENT,

                    @SerializedName("service")
                    SERVICE
                }
            }
        }
    }
}