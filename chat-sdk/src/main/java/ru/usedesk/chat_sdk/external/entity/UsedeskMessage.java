package ru.usedesk.chat_sdk.external.entity;

import android.text.Html;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UsedeskMessage {
    private static final String KEY_FILE = "file";

    private String id;
    private UsedeskMessageType type;
    private String text;
    private String operator;
    private String createdAt;
    private String name;
    private Object chat;
    private UsedeskMessageButtons messageButtons;

    @SerializedName(KEY_FILE)
    private UsedeskFile usedeskFile;

    private UsedeskPayload payload;

    @NonNull
    public UsedeskMessageButtons getMessageButtons() {
        if (messageButtons == null) {
            messageButtons = new UsedeskMessageButtons(Html.fromHtml(Html.fromHtml(text == null ? "" : text).toString()).toString());
        }
        return messageButtons;
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
        return getMessageButtons().getMessageText();
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

    public UsedeskFile getUsedeskFile() {
        return usedeskFile;
    }

    public Object getPayloadAsObject() {
        return payload;
    }

    public UsedeskPayload getPayload() {
        return payload;
        /*if (payload != null) {
            try {
                Gson gson = new Gson();
                if (payload instanceof LinkedTreeMap) {
                    JsonObject jsonObject = gson.toJsonTree(payload).getAsJsonObject();
                    if (jsonObject != null) {
                        return gson.fromJson(jsonObject, UsedeskPayload.class);
                    }
                }
                return gson.fromJson(payload.toString(), UsedeskPayload.class);
            } catch (JsonSyntaxException e) {
                return new UsedeskPayload();
            }
        } else {
            return new UsedeskPayload();
        }*/
    }
}