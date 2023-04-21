
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

data class UsedeskMessageClientFile @JvmOverloads constructor(
    override val id: Long,
    override val createdAt: Calendar,
    override val file: UsedeskFile,
    override val status: UsedeskMessageOwner.Client.Status,
    override val localId: Long = id
) : UsedeskMessage.File, UsedeskMessageOwner.Client