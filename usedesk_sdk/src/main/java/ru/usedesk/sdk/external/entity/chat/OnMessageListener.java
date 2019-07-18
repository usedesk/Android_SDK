package ru.usedesk.sdk.external.entity.chat;

public interface OnMessageListener {

    void onNew(Message message);

    void onFeedback();

    void onInit(String token, Setup setup);

    void onInitChat();

    void onTokenError();
}
