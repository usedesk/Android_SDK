package ru.usedesk.chat_sdk.internal.domain.entity

internal class Message {
    var payload: Payload? = null
    var id: String? = null
    var type: String? = null
    var text: String? = null
    var operator: String? = null
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
        var buttons: List<Button?>? = null
        var isCsi: Boolean? = null
        var userRating: String? = null
        var avatar: String? = null

        class Button {
            var type: String? = null
            var title: String? = null
            var icon: Icon? = null
            var data: String? = null

            enum class Icon {
                LIKE, DISLIKE
            }
        }
    }

    companion object {
        const val TYPE_OPERATOR_TO_CLIENT = "operator_to_client"
        const val TYPE_CLIENT_TO_OPERATOR = "client_to_operator"
        const val TYPE_CLIENT_TO_BOT = "client_to_bot"
        const val TYPE_BOT_TO_CLIENT = "bot_to_client"
        const val SERVICE = "service"
    }
}