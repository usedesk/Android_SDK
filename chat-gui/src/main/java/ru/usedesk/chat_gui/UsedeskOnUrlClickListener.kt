package ru.usedesk.chat_gui

interface UsedeskOnUrlClickListener {
    fun onUrlClick(url: String)
}

@Deprecated(
    message = "Use ru.usedesk.chat_gui.UsedeskOnUrlClickListener",
    replaceWith = ReplaceWith(
        "UsedeskOnUrlClickListener",
        "ru.usedesk.chat_gui.UsedeskOnUrlClickListener"
    )
)
typealias IUsedeskOnUrlClickListener = UsedeskOnUrlClickListener
