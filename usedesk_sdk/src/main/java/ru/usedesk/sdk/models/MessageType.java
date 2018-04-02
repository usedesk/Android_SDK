package ru.usedesk.sdk.models;

import com.google.gson.annotations.SerializedName;

public enum MessageType {

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