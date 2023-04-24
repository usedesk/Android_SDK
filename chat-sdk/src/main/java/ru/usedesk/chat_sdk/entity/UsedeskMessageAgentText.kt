
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

data class UsedeskMessageAgentText(
    override val id: Long,
    override val createdAt: Calendar,
    override val text: String,
    override val convertedText: String,
    override val name: String,
    override val avatar: String,
    val feedbackNeeded: Boolean,
    val feedback: UsedeskFeedback?,
    val buttons: List<Button>,
    val fieldsInfo: List<FieldInfo>
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