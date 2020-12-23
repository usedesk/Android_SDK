package ru.usedesk.chat_sdk.external.entity;

import java.util.ArrayList;
import java.util.List;

public class UsedeskMessageButtons {
    private final String messageText;
    private final List<UsedeskMessageButton> messageButtons = new ArrayList<>();

    UsedeskMessageButtons(String messageText) {
        if (messageText != null) {
            while (messageText.contains("{{button:") && messageText.contains("}}")) {
                int start = messageText.indexOf("{{button:");
                int end = messageText.indexOf("}}");

                //Выделим секцию кнопки
                String buttonText = messageText.substring(start, end + 2);

                UsedeskMessageButton messageButton = new UsedeskMessageButton(buttonText);

                String replaceBy;
                if (messageButton.isShow()) {
                    replaceBy = messageButton.getText();
                } else {
                    replaceBy = "";
                }
                //Удалим её из исходного сообщения
                messageText = messageText.replace(buttonText, replaceBy);

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
