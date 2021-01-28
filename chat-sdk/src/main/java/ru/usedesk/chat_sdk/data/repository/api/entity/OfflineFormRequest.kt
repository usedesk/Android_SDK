package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class OfflineFormRequest(
        @SerializedName("company_id")
        val companyId: String,
        val name: String,
        val email: String,
        val message: String
)