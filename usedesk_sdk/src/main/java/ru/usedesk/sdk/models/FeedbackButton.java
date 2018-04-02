package ru.usedesk.sdk.models;

import com.google.gson.annotations.SerializedName;

public class FeedbackButton {

    private String type;
    private String title;
    private Icon icon;
    private String data;

    public FeedbackButton() {
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

    private static enum Icon {

        @SerializedName("like")
        LIKE,

        @SerializedName("dislike")
        DISLIKE
    }
}