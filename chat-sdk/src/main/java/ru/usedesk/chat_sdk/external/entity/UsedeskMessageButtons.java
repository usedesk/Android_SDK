package ru.usedesk.chat_sdk.external.entity;

import java.util.ArrayList;
import java.util.List;

public class UsedeskMessageButtons {
    private final String messageText;
    private final List<UsedeskMessageButton> messageButtons = new ArrayList<>();

    UsedeskMessageButtons(String messageText) {
        if (messageText != null) {
            String tempText = messageText;
            while (tempText.contains("{{button:") && tempText.contains("}}")) {
                int start = tempText.indexOf("{{button:");
                int end = tempText.indexOf("}}", start);

                //Выделим секцию кнопки
                String buttonText = tempText.substring(start, end + 2);

                UsedeskMessageButton messageButton = UsedeskMessageButton.create(buttonText);

                if (messageButton != null) {
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

                tempText = tempText.substring(end);
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
