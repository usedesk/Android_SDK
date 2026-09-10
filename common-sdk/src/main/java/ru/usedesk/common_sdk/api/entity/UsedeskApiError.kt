
package ru.usedesk.common_sdk.api.entity

import com.google.gson.annotations.SerializedName

open class UsedeskApiError {
    @SerializedName("code") val code: Int? = null
    @SerializedName("error") val error: String? = null
}