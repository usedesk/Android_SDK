
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

data class UsedeskMessageClientAudio @JvmOverloads constructor(
    override val id: String,
    override val createdAt: Calendar,
    override val file: UsedeskFile,
    override val status: UsedeskMessageOwner.Client.Status,
    override val localId: String = id
) : UsedeskMessage.File, UsedeskMessageOwner.Client