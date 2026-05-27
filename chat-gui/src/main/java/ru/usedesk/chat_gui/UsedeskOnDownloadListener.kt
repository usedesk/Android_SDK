package ru.usedesk.chat_gui

interface UsedeskOnDownloadListener {
    fun onDownload(url: String, name: String)
}

@Deprecated(
    message = "Use ru.usedesk.chat_gui.UsedeskOnDownloadListener",
    replaceWith = ReplaceWith(
        "UsedeskOnDownloadListener",
        "ru.usedesk.chat_gui.UsedeskOnDownloadListener"
    )
)
typealias IUsedeskOnDownloadListener = UsedeskOnDownloadListener
