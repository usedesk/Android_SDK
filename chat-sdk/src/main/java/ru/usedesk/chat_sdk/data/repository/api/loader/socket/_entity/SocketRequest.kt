
package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity

import com.google.gson.annotations.SerializedName

internal sealed class SocketRequest(@SerializedName("type") val type: String) {
    class Init(
        @SerializedName("token") val token: String?,
        @SerializedName("company_id") val companyId: String,
        @SerializedName("url") val url: String,
        messageLimit: Int?,
        userData: Payload.UserData,
        version: String
    ) : SocketRequest(TYPE) {

        @SerializedName("payload") private val payload = Payload(messageLimit, userData, version)

        class Payload(
            @SerializedName("message_limit") val messageLimit: Int?,
            @SerializedName("userData") val userData: UserData,
            @SerializedName("version") val version: String
        ) {
            @SerializedName("sdk") val sdk = VALUE_CURRENT_SDK
            @SerializedName("type") val type = VALUE_TYPE_SDK

            class UserData(
                @SerializedName("device") val device: String,
                @SerializedName("os") val os: String,
                @SerializedName("app_name") val appName: String,
                @SerializedName("app_version") val appVersion: String,
                @SerializedName("mobile_operator_name") val mobileOperatorName: String,
                @SerializedName("sdk_version") val sdkVersion: String
            )
        }

        companion object {
            private const val TYPE = "@@server/chat/INIT"
            private const val VALUE_CURRENT_SDK = "android"
            private const val VALUE_TYPE_SDK = "sdk"
        }
    }

    class SendMessage(
        text: String,
        messageId: String
    ) : SocketRequest(TYPE) {

        @SerializedName("message") private val message = RequestMessage(text, messageId)

        companion object {
            private const val TYPE = "@@server/chat/SEND_MESSAGE"
        }

        private class RequestMessage(
            @SerializedName("text") private val text: String,
            messageId: String
        ) {
            @SerializedName("payload") private val payload = Payload(messageId)
        }

        private class Payload(
            @SerializedName("message_id") private val messageId: String
        )
    }

    class Rating(
        messageId: String,
        ratingData: String
    ) : SocketRequest(TYPE) {

        @SerializedName("payload") private val payload = Payload(ratingData, messageId)

        private class Payload(
            @SerializedName("data") private val data: String,
            @SerializedName("message_id") private val messageId: String
        ) {
            @SerializedName("type") private val type: String = VALUE_FEEDBACK_ACTION
        }

        companion object {
            private const val TYPE = "@@server/chat/CALLBACK"
            private const val VALUE_FEEDBACK_ACTION = "action"
        }
    }
}
