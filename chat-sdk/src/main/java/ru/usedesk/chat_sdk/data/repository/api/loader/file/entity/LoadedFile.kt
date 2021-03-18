package ru.usedesk.chat_sdk.data.repository.api.loader.file.entity

internal class LoadedFile(
        val name: String,
        val size: Int,
        val type: String,
        val bytes: ByteArray
)