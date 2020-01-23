package ru.usedesk.chat_sdk.external.entity;

import com.google.gson.annotations.SerializedName;

public class UsedeskFeedbackButton {
    private String type;
    private String title;
    private Icon icon;
    private String data;

    public UsedeskFeedbackButton() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private enum Icon {
        @SerializedName("like")
        LIKE,
        @SerializedName("dislike")
        DISLIKE
    }
}