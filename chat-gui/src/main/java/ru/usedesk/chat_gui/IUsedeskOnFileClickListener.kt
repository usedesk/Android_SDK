package ru.usedesk.chat_gui

import ru.usedesk.chat_sdk.data._entity.UsedeskFile

interface IUsedeskOnFileClickListener {
    fun onFileClick(usedeskFile: UsedeskFile)
}