package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;

public class UsedeskMessageButton {
    private final String text;
    private final String url;
    private final String type;
    private final boolean show;

    UsedeskMessageButton(@NonNull String messageButtonText) {
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
