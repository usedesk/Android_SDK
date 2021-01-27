package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class FileResponse {
    var status: Int? = null

    @SerializedName("file_link")
    var fileLink: String? = null
    var size: String? = null
    var id: String? = null
    var type: String? = null
    var name: String? = null
}