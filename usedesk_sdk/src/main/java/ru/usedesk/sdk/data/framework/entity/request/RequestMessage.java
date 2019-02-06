package ru.usedesk.sdk.data.framework.entity.request;

import ru.usedesk.sdk.domain.entity.UsedeskFile;

public class RequestMessage {

    private String text;

    private UsedeskFile file;

    public RequestMessage() {
    }

    public RequestMessage(String text, UsedeskFile file) {
        this.text = text;
        this.file = file;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UsedeskFile getUsedeskFile() {
        return file;
    }

    public void setUsedeskFile(UsedeskFile usedeskFile) {
        this.file = usedeskFile;
    }
}