package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UsedeskMessageButton {
    private final String text;
    private final String url;
    private final String type;
    private final boolean show;

    @Deprecated
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

    public UsedeskMessageButton(@NonNull String text,
                                @NonNull String url,
                                @NonNull String type,
                                boolean show) {
        this.text = text;
        this.url = url;
        this.type = type;
        this.show = show;
    }

    @Nullable
    public static UsedeskMessageButton create(@NonNull String messageButtonText) {
        String[] sections = messageButtonText.replace("{{button:", "")
                .replace("}}", "")
                .split(";");

        if (sections.length == 4) {
            return new UsedeskMessageButton(
                    sections[0],
                    sections[1],
                    sections[2],
                    sections[3].equals("show")
            );
        } else {
            return null;
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
