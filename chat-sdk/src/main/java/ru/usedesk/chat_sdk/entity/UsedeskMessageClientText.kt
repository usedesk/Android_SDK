
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

data class UsedeskMessageClientText @JvmOverloads constructor(
    override val id: Long,
    override val createdAt: Calendar,
    override val text: String,
    override val convertedText: String,
    override val status: UsedeskMessageOwner.Client.Status,
    override val localId: Long = id
) : UsedeskMessage.Text, UsedeskMessageOwner.Client