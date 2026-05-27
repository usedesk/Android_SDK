package ru.usedesk.knowledgebase_gui.screen

interface UsedeskOnSupportClickListener {
    fun onSupportClick()
}

@Deprecated(
    message = "Use ru.usedesk.knowledgebase_gui.screen.UsedeskOnSupportClickListener",
    replaceWith = ReplaceWith(
        "UsedeskOnSupportClickListener",
        "ru.usedesk.knowledgebase_gui.screen.UsedeskOnSupportClickListener"
    )
)
typealias IUsedeskOnSupportClickListener = UsedeskOnSupportClickListener
