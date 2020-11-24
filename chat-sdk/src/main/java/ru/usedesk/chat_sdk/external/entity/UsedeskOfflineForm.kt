package ru.usedesk.chat_sdk.external.entity

import com.google.gson.annotations.SerializedName

data class UsedeskOfflineForm(
        @SerializedName("company_id")
        val companyId: String?,
        val name: String?,
        val email: String?,
        val message: String?)