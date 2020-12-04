package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.Converter
import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.entity.FileResponse
import ru.usedesk.chat_sdk.entity.UsedeskChatItem
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientFile
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientImage
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
internal class FileResponseConverter : Converter<FileResponse, UsedeskChatItem>() {

    override fun convert(from: FileResponse): UsedeskChatItem {
        val file = UsedeskFile(
                from.fileLink!!,
                from.type!!,
                from.size!!,
                from.name!!
        )
        val id = from.id!!.toLong()
        return if (file.isImage()) {
            UsedeskMessageClientImage(id, Calendar.getInstance(), file)
        } else {
            UsedeskMessageClientFile(id, Calendar.getInstance(), file)
        }
    }
}