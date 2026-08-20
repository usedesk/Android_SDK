
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

data class UsedeskMessageAgentText @JvmOverloads constructor(
    override val id: String,
    override val createdAt: Calendar,
    override val text: String,
    override val convertedText: String,
    override val name: String,
    override val avatar: String,
    val buttons: List<Button>,
    val fieldsInfo: List<FieldInfo>,
    val rating: UsedeskRating? = null
) : UsedeskMessage.Text, UsedeskMessageOwner.Agent {

    data class Button(
        val name: String = "",
        val url: String = "",
        val type: String = ""
    )

    class FieldInfo(
        val id: String,
        val name: String,
        val required: Boolean
    )
}
