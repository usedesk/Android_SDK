package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse

internal class MessageResponse : BaseResponse() {

    var message: Message? = null

    companion object {
        const val TYPE = "@@chat/current/ADD_MESSAGE"

        const val TYPE_OPERATOR_TO_CLIENT = "operator_to_client"
        const val TYPE_CLIENT_TO_OPERATOR = "client_to_operator"
        const val TYPE_CLIENT_TO_BOT = "client_to_bot"
        const val TYPE_BOT_TO_CLIENT = "bot_to_client"
    }

    class Message {
        var payload: Payload? = null
        var id: Long? = null
        var type: String? = null
        var text: String? = null

        @SerializedName("createdAt")
        var createdAt: String? = null
        var name: String? = null
        var chat: Any? = null
        var file: File? = null

        class File {
            var name: String? = null
            var type: String? = null
            var content: String? = null
            var size: String? = null
        }

        class Payload {
            var buttons: Array<Button?>? = null

            @SerializedName("userRating")
            var userRating: String? = null
            var avatar: String? = null
            var messageId: Long? = null

            class Button {
                var type: String? = null
                var icon: String? = null
                var data: String? = null
            }
        }
    }
}