package ru.usedesk.chat_sdk.entity

interface UsedeskMessageClient {

    val status: Status

    enum class Status {
        SENDING,
        SUCCESSFULLY_SENT,
        SEND_FAILED,
        RECEIVED
    }
}