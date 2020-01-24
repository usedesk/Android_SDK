package ru.usedesk.chat_sdk.internal.domain.entity;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageType;

public class BaseMessage {
    private String id;
    private UsedeskMessageType type;
    private String text;
    private String operator;
    private String createdAt;
    private String name;
    private Object chat;
    private UsedeskFile file;

    public BaseMessage() {
    }

    public BaseMessage(@NonNull BaseMessage baseMessage) {
        this.id = baseMessage.getId();
        this.type = baseMessage.getType();
        this.text = baseMessage.getText();
        this.operator = baseMessage.getOperator();
        this.createdAt = baseMessage.getCreatedAt();
        this.name = baseMessage.getName();
        this.chat = baseMessage.getChat();
        this.file = baseMessage.getFile();
    }

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public UsedeskMessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getOperator() {
        return operator;
    }

    public String getName() {
        return name;
    }

    public Object getChat() {
        return chat;
    }

    public UsedeskFile getFile() {
        return file;
    }
}