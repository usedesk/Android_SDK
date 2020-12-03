package ru.usedesk.chat_sdk.data.framework.fileinfo

import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException

interface IFileInfoLoader {
    @Throws(UsedeskException::class)
    fun getFrom(fileInfo: UsedeskFileInfo): UsedeskFile
}