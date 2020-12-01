package ru.usedesk.chat_sdk.external.entity.old

class UsedeskMessageButtons internal constructor(
        val messageText: String
) {
    private val messageButtons: MutableList<UsedeskMessageButton> = mutableListOf()

    fun getMessageButtons(): List<UsedeskMessageButton> {
        return messageButtons
    }

    init {
        var text = messageText
        while (text.contains("{{button:") && text.contains("}}")) {
            val start = text.indexOf("{{button:")
            val end = text.indexOf("}}")

            //Выделим секцию кнопки
            val buttonText = text.substring(start, end + 2)

            //Удалим её из исходного сообщения
            text = text.replace(buttonText, "")
            val messageButton = UsedeskMessageButton(buttonText)
            if (!messageButton.isShow) {
                text = text.replace(messageButton.text, "")
            }
            messageButtons.add(messageButton)
        }
    }
}