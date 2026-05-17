package ru.usedesk.chat_gui

import ru.usedesk.chat_sdk.entity.UsedeskFile

interface UsedeskOnFileClickListener {
    fun onFileClick(usedeskFile: UsedeskFile)
}

@Deprecated(
    message = "Use ru.usedesk.chat_gui.UsedeskOnFileClickListener",
    replaceWith = ReplaceWith(
        "UsedeskOnFileClickListener",
        "ru.usedesk.chat_gui.UsedeskOnFileClickListener"
    )
)
typealias IUsedeskOnFileClickListener = UsedeskOnFileClickListener
