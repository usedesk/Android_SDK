package ru.usedesk.chat_sdk.external.entity

class UsedeskMessageButton(
        messageButtonText: String
) {

    val text: String
    val url: String
    val type: String
    val isShow: Boolean

    init {
        val sections = messageButtonText.replace("{{button:", "")
                .replace("}}", "")
                .split(";")
        if (sections.size == 4) {
            text = sections[0]
            url = sections[1]
            type = sections[2]
            isShow = sections[3] == "show"
        } else {
            text = ""
            url = ""
            type = ""
            isShow = true
        }
    }
}