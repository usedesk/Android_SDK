package ru.usedesk.knowledgebase_gui.screen

interface UsedeskOnWebUrlListener {
    fun onWebUrl(url: String)
}

@Deprecated(
    message = "Use ru.usedesk.knowledgebase_gui.screen.UsedeskOnWebUrlListener",
    replaceWith = ReplaceWith(
        "UsedeskOnWebUrlListener",
        "ru.usedesk.knowledgebase_gui.screen.UsedeskOnWebUrlListener"
    )
)
typealias IUsedeskOnWebUrlListener = UsedeskOnWebUrlListener
