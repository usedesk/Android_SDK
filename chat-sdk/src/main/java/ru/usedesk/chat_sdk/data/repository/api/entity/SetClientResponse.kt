package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class SetClientResponse(
    @SerializedName("client_id")
    private val clientId: Long
)