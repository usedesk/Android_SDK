package ru.usedesk.chat_gui

interface UsedeskOnAttachmentClickListener {
    fun onAttachmentClick()
}

@Deprecated(
    message = "Use ru.usedesk.chat_gui.UsedeskOnAttachmentClickListener",
    replaceWith = ReplaceWith(
        "UsedeskOnAttachmentClickListener",
        "ru.usedesk.chat_gui.UsedeskOnAttachmentClickListener"
    )
)
typealias IUsedeskOnAttachmentClickListener = UsedeskOnAttachmentClickListener
