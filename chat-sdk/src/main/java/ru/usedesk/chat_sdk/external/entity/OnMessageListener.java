package ru.usedesk.chat_sdk.external.entity;

import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup;

public interface OnMessageListener {

    void onNew(Message message);

    void onFeedback();

    void onInit(String token, Setup setup);

    void onInitChat();

    void onTokenError();
}
