
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

data class UsedeskMessageAgentAudio(
    override val id: Long,
    override val createdAt: Calendar,
    override val file: UsedeskFile,
    override val name: String,
    override val avatar: String
) : UsedeskMessage.File, UsedeskMessageOwner.Agent