package ru.usedesk.chat_sdk.internal.domain.entity;

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup;

public interface OnMessageListener {

    void onInit(String token, Setup setup);

    void onInitChat();

    void onNew(UsedeskMessage message);

    void onFeedback();

    void onTokenError();

    void onSetEmailSuccess();
}
