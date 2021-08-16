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
        var noOperators: Boolean? = null
        var messages: List<MessageResponse.Message?>? = null
        var ticket: Ticket? = null

        @SerializedName("callback_settings")
        var callbackSettings: CallbackSettings? = null

        class CallbackSettings {
            @SerializedName("work_type")
            var workType: String? = null

            @SerializedName("callback_title")
            var callbackTitle: String? = null

            @SerializedName("callback_greeting")
            var callbackGreeting: String? = null

            @SerializedName("custom_fields")
            var customFields: Array<CustomField?>? = null

            var topics: Array<Topic?>? = null

            @SerializedName("topics_title")
            var topicsTitle: String? = null

            @SerializedName("topics_required")
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

            @SerializedName("status_id")
            var statusId: Int? = null
        }
    }
}