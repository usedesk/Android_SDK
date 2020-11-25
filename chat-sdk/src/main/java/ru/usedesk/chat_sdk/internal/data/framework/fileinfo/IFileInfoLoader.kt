package ru.usedesk.chat_sdk.internal.data.framework.fileinfo

import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

interface IFileInfoLoader {
    @Throws(UsedeskException::class)
    fun getFrom(fileInfo: UsedeskFileInfo): UsedeskFile
}