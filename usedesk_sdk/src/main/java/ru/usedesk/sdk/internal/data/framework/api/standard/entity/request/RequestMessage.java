package ru.usedesk.sdk.internal.data.framework.api.standard.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.external.entity.chat.UsedeskFile;

public class RequestMessage {
    private static final String KEY_FILE = "file";

    private String text;

    @SerializedName(KEY_FILE)
    private UsedeskFile usedeskFile;

    public RequestMessage() {
    }

    public RequestMessage(String text, UsedeskFile usedeskFile) {
        this.text = text;
        this.usedeskFile = usedeskFile;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UsedeskFile getUsedeskFile() {
        return usedeskFile;
    }

    public void setUsedeskFile(UsedeskFile usedeskFile) {
        this.usedeskFile = usedeskFile;
    }
}