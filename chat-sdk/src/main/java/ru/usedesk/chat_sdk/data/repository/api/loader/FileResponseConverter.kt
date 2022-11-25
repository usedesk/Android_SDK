package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.repository._extra.Converter
import ru.usedesk.chat_sdk.data.repository.api.entity.SendFile
import ru.usedesk.chat_sdk.entity.*
import java.util.*

internal class FileResponseConverter : Converter<SendFile.Response, UsedeskMessage> {

    override fun convert(from: SendFile.Response): UsedeskMessage {
        val file = UsedeskFile.create(
            from.fileLink!!,
            from.type,
            from.size!!,
            from.name!!
        )
        val id = from.id!!.toLong()
        val createdAt = Calendar.getInstance()
        return when {
            file.isImage() -> {
                UsedeskMessageClientImage(
                    id,
                    createdAt,
                    file,
                    UsedeskMessageClient.Status.SUCCESSFULLY_SENT
                )
            }
            file.isVideo() -> {
                UsedeskMessageClientVideo(
                    id,
                    createdAt,
                    file,
                    UsedeskMessageClient.Status.SUCCESSFULLY_SENT
                )
            }
            file.isAudio() -> {
                UsedeskMessageClientAudio(
                    id,
                    createdAt,
                    file,
                    UsedeskMessageClient.Status.SUCCESSFULLY_SENT
                )
            }
            else -> {
                UsedeskMessageClientFile(
                    id,
                    createdAt,
                    file,
                    UsedeskMessageClient.Status.SUCCESSFULLY_SENT
                )
            }
        }
    }
}