package ru.usedesk.sdk.external.entity.chat;

import java.util.ArrayList;
import java.util.List;

public class MessageWithButtons {
    private String text;
    private List<String> buttons;

    public MessageWithButtons(String text, List<String> buttons) {
        this.text = text;
        this.buttons = buttons;
    }

    public MessageWithButtons(String text) {
        this.buttons = new ArrayList<>();

        if (text != null) {
            while (text.contains("{{") && text.contains("}}")) {
                int start = text.indexOf("{{button:");
                int end = text.indexOf("show}}");

                //Выделим секцию кнопки
                String button = text.substring(start, end + 6);

                //Удалим её из исходного сообщения
                text = text.replace(button, "");

                boolean noshow = button.endsWith(";;;noshow}}");

                button = button.replace("{{button:", "")
                        .replace(";;;noshow}}", "")
                        .replace(";;;show}}", "");


                if (noshow) {
                    text = text.replace(button, "");
                }
                buttons.add(button);
            }
        }

        this.text = text;
    }

    public String getText() {
        return text;
    }

    public List<String> getButtons() {
        return buttons;
    }
}
