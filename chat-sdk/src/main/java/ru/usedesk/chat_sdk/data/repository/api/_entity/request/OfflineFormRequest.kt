package ru.usedesk.chat_sdk.data.repository.api._entity.request

import com.google.gson.annotations.SerializedName
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm

class OfflineFormRequest(
        @SerializedName("company_id")
        val companyId: String,
        val name: String,
        val email: String,
        val message: String
) {
    constructor(
            companyId: String,
            offlineForm: UsedeskOfflineForm
    ) : this(companyId,
            offlineForm.name,
            offlineForm.email,
            offlineForm.message)
}