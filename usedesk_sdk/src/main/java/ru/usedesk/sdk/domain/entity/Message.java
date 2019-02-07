package ru.usedesk.sdk.domain.entity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import static ru.usedesk.sdk.domain.entity.Constants.KEY_FILE;

public class Message {

    private String id;
    private MessageType type;
    private String text;
    private String operator;
    private String createdAt;
    private String name;
    private Object chat;

    @SerializedName(KEY_FILE)
    private UsedeskFile usedeskFile;

    private Object payload;

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, String text) {
        this(type);
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public UsedeskFile getUsedeskFile() {
        return usedeskFile;
    }

    public void setUsedeskFile(UsedeskFile usedeskFile) {
        this.usedeskFile = usedeskFile;
    }

    public Payload getPayload() {
        if (payload != null) {
            try {
                Gson gson = new Gson();
                if (payload instanceof LinkedTreeMap) {
                    JsonObject jsonObject = gson.toJsonTree(payload).getAsJsonObject();
                    if (jsonObject != null) {
                        return gson.fromJson(jsonObject, Payload.class);
                    }
                }
                return gson.fromJson(payload.toString(), Payload.class);//TODO: переместить gson
            } catch (JsonSyntaxException e) {
                return new Payload();
            }
        } else {
            return new Payload();
        }
    }
}