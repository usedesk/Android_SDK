
package ru.usedesk.chat_sdk.entity

import java.util.Calendar

sealed interface UsedeskMessage {
    val id: String
    val createdAt: Calendar

    sealed interface Text : UsedeskMessage {
        val text: String
        val convertedText: String
    }

    sealed interface File : UsedeskMessage {
        val file: UsedeskFile
    }
}