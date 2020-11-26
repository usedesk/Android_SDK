package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request

import com.google.gson.annotations.SerializedName

class InitChatRequest(
        token: String?,
        @SerializedName(KEY_COMPANY_ID)
        val companyId: String,
        val url: String
) : BaseRequest(TYPE, token ?: "") {

    private val payload: Payload = Payload()

    class Payload {
        val sdk: String = VALUE_CURRENT_SDK
    }

    companion object {
        private const val TYPE = "@@server/chat/INIT"
        private const val KEY_COMPANY_ID = "company_id"
        private const val VALUE_CURRENT_SDK = "android"
    }
}