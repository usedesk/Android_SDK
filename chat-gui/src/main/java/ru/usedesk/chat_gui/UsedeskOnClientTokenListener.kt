package ru.usedesk.chat_gui

interface UsedeskOnClientTokenListener {
    fun onClientToken(clientToken: String)
}

@Deprecated(
    message = "Use ru.usedesk.chat_gui.UsedeskOnClientTokenListener",
    replaceWith = ReplaceWith(
        "UsedeskOnClientTokenListener",
        "ru.usedesk.chat_gui.UsedeskOnClientTokenListener"
    )
)
typealias IUsedeskOnClientTokenListener = UsedeskOnClientTokenListener
