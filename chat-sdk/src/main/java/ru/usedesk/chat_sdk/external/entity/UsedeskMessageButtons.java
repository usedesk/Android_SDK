package ru.usedesk.chat_sdk.external.entity;

import java.util.ArrayList;
import java.util.List;

public class UsedeskMessageButtons {
    private final String messageText;
    private final List<UsedeskMessageButton> messageButtons;

    UsedeskMessageButtons(String messageText) {
        this.messageButtons = new ArrayList<>();

        if (messageText != null) {
            while (messageText.contains("{{button:") && messageText.contains("}}")) {
                int start = messageText.indexOf("{{button:");
                int end = messageText.indexOf("}}");

                //Выделим секцию кнопки
                String buttonText = messageText.substring(start, end + 2);

                //Удалим её из исходного сообщения
                messageText = messageText.replace(buttonText, "");

                UsedeskMessageButton messageButton = new UsedeskMessageButton(buttonText);

                if (!messageButton.isShow()) {
                    messageText = messageText.replace(messageButton.getText(), "");
                }

                messageButtons.add(messageButton);
            }
        }

        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    public List<UsedeskMessageButton> getMessageButtons() {
        return messageButtons;
    }

}
