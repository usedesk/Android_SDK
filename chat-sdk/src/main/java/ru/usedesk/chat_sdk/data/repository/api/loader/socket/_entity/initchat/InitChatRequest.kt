package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class InitChatRequest(
        token: String?,
        @SerializedName(KEY_COMPANY_ID)
        val companyId: String,
        val url: String
) : BaseRequest(TYPE, token ?: "") {

    private val payload: Payload = Payload()

    class Payload {
        val sdk: String = VALUE_CURRENT_SDK
        val type: String = VALUE_TYPE_SDK
    }

    companion object {
        private const val TYPE = "@@server/chat/INIT"
        private const val KEY_COMPANY_ID = "company_id"
        private const val VALUE_CURRENT_SDK = "android"
        private const val VALUE_TYPE_SDK = "sdk"
    }
}