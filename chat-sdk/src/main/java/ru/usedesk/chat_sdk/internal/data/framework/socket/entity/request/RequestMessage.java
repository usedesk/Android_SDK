package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskFile;

public class RequestMessage {

    private final String text;
    private final UsedeskFile file;

    public RequestMessage(@NonNull String text) {
        this.text = text;
        this.file = null;
    }

    public RequestMessage(@NonNull UsedeskFile usedeskFile) {
        this.text = null;
        this.file = usedeskFile;
    }

    public String getText() {
        return text;
    }

    public UsedeskFile getUsedeskFile() {
        return file;
    }
}