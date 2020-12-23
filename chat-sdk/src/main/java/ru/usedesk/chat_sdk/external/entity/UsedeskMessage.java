package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage;

public class UsedeskMessage extends BaseMessage {
    private UsedeskPayload usedeskPayload;
    private String stringPayload;

    private UsedeskMessageButtons messageButtons;

    public UsedeskMessage(@NonNull BaseMessage baseMessage,
                          @Nullable UsedeskPayload usedeskPayload,
                          @Nullable String stringPayload) {
        super(baseMessage);
        this.usedeskPayload = usedeskPayload;
        this.stringPayload = stringPayload;

        String text = baseMessage.getText() == null ? "" : baseMessage.getText();
        text = text.replace("\r\n", "<br />")
                .replace("\n\n", "<br />")
                .replace("\n", "<br />");
        messageButtons = new UsedeskMessageButtons(text);
    }

    @NonNull
    public UsedeskMessageButtons getMessageButtons() {
        return messageButtons;
    }

    @Override
    public String getText() {
        return getMessageButtons().getMessageText();
    }

    public UsedeskPayload getUsedeskPayload() {
        return usedeskPayload;
    }

    public String getStringPayload() {
        return stringPayload;
    }
}