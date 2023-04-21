
package ru.usedesk.chat_sdk.entity

sealed interface UsedeskMessageOwner {
    sealed interface Agent : UsedeskMessageOwner {
        val name: String
        val avatar: String
    }

    sealed interface Client : UsedeskMessageOwner {
        val status: Status
        val localId: Long

        enum class Status {
            SENDING,
            SUCCESSFULLY_SENT,
            SEND_FAILED
        }
    }
}