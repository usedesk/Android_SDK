package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse

internal class InitChatResponse : BaseResponse() {
    var token: String? = null
    var setup: Setup? = null

    companion object {
        const val TYPE = "@@chat/current/INITED"
    }

    class Setup {
        @SerializedName("noOperators")
        var noOperators: Boolean? = null
        var messages: List<MessageResponse.Message?>? = null
        var ticket: Ticket? = null
        var callbackSettings: CallbackSettings? = null

        class CallbackSettings {
            var workType: String? = null
            var callbackTitle: String? = null
            var callbackGreeting: String? = null
            var customFields: Array<CustomField?>? = null
            var topics: Array<Topic?>? = null
            var topicsTitle: String? = null
            var topicsRequired: Int? = null

            class CustomField {
                var type: String? = null
                var placeholder: String? = null
                var checked: Boolean? = null
                var required: Boolean? = null
            }

            class Topic {
                var text: String? = null
                var checked: Boolean? = null
            }
        }

        class Ticket {
            var id: Long? = null
            var statusId: Int? = null
        }
    }
}