package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MessageButtons {
    private final String messageText;
    private final List<MessageButton> messageButtons;

    MessageButtons(String messageText) {
        this.messageButtons = new ArrayList<>();

        if (messageText != null) {
            while (messageText.contains("{{button:") && messageText.contains("}}")) {
                int start = messageText.indexOf("{{button:");
                int end = messageText.indexOf("}}");

                //Выделим секцию кнопки
                String buttonText = messageText.substring(start, end + 2);

                //Удалим её из исходного сообщения
                messageText = messageText.replace(buttonText, "");

                MessageButton messageButton = new MessageButton(buttonText);

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

    public List<MessageButton> getMessageButtons() {
        return messageButtons;
    }

    public class MessageButton {
        private final String text;
        private final String url;
        private final String type;
        private final boolean show;

        MessageButton(@NonNull String messageButtonText) {
            String[] sections = messageButtonText.replace("{{button:", "")
                    .replace("}}", "")
                    .split(";");

            if (sections.length == 4) {
                this.text = sections[0];
                this.url = sections[1];
                this.type = sections[2];
                this.show = sections[3].equals("show");
            } else {
                this.text = "";
                this.url = "";
                this.type = "";
                this.show = true;
            }
        }

        @NonNull
        public String getText() {
            return text;
        }

        @NonNull
        public String getUrl() {
            return url;
        }

        @NonNull
        public String getType() {
            return type;
        }

        public boolean isShow() {
            return show;
        }
    }
}
