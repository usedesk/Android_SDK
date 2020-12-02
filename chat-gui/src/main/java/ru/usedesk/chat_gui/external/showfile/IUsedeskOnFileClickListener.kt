package ru.usedesk.chat_gui.external.showfile

import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile

interface IUsedeskOnFileClickListener {
    fun onFileClick(usedeskFile: UsedeskFile)
}